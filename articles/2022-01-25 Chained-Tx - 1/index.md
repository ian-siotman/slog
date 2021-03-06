---
title: "Chained-Tx & QDSL - 1"
date: 2022-01-25 03:30:00
---

메인서버와 채권관리서버 사이의 게이트웨이 서버를 신설하였다. 

여기서 api 개발 및 각 서버가 사용하는 mysql 과 psql 서로간의 동기화를 위한 트랜젝션을 설정하였다.

트랜젝션 설정을 어떻게 했고, 어떤 경우에 사용했는지 기록하기 위해 문서를 남긴다.

이 글에서는 기본적인 설정 코드를 단순 나열한다.

이후 이어지는 글에서는 각 설정의 의미와 내부 동작을 쪼개어 살핀다.

### Db01 (가칭) 및 Db02, 그리고 TxManager Config. 코드들

이하는 TransactionManagerConfig.kt 이다. 

미리 스포를 하자면 `ChainedTransactionManager` 는 Deprecated 되었고, 이에 대한 대응도 고민해볼 것이다.

```kotlin
@Configuration
class TransactionManagerConfig {
    companion object {
        const val chainedTxManagerQualifier = "chainedTransactionManager"
    }

    @Bean
    // 메소드 이름으로 Bean Name 이 들어갈테니, 명시적으로 지정하진 않았지만, 
    // 지금보니 아래 Db01DataSourceConfig 에서는 명시했다. 컨벤션이 없을 때 & 한 레포 내 코드라면 통일하는게 맞아보인다.  
    fun chainedTransactionManager(
        @Qualifier(Db01Props.transactionManager) db01TransactionManager: PlatformTransactionManager,
        @Qualifier(Db02Props.transactionManager) db02TransactionManager: PlatformTransactionManager
    ) = ChainedTransactionManager(db01TransactionManager, db02TransactionManager)
}
```

```kotlin
object Db01Props {
    // 상수 값들에 고유함을 부여하기 위해 identifier 만 따로 정의하였다.
    private const val identifier = "db01"

    // application.yml 상 경로
    const val configPropsPrefix = "spring.datasource.$identifier"

    // Qualifier 등지에서 땡겨쓸 수 있도록 public 으로 두었다. 
    const val basePackage = "kr.co.db01.domain.$identifier"
    const val dataSource = "${identifier}DataSource"
    const val persistenceUnit = "${identifier}PersistenceUnit"
    const val transactionManager = "${identifier}TransactionManager"
    const val entityManagerFactory = "${identifier}EntityManagerFactory"
}

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = [Db01Props.basePackage],
    transactionManagerRef = Db01Props.transactionManager,
    entityManagerFactoryRef = Db01Props.entityManagerFactory
)
class Db01DataSourceConfig(private val env: Environment) {

    @Bean(name = [Db01Props.dataSource])
    @ConfigurationProperties(Db01Props.configPropsPrefix)
    fun dataSource(): DataSource = DataSourceBuilder.create().build()

    @Bean(name = [Db01Props.entityManagerFactory])
    fun entityManagerFactory() = LocalContainerEntityManagerFactoryBean().apply {
        this.dataSource = dataSource()
        this.jpaVendorAdapter = jpaVendorAdapter()
        this.persistenceUnitName = Db01Props.persistenceUnit

        this.setPackagesToScan(Db01Props.basePackage)
    }

    @Bean(name = [Db01Props.transactionManager])
    fun transactionManager() = JpaTransactionManager().apply {
        this.entityManagerFactory = entityManagerFactory().`object`
    }

    private fun jpaVendorAdapter() = HibernateJpaVendorAdapter().apply {
        // 초기 db02 의 개발계는 ncp, 우리 db01 의 개발계는 aws 라서, development-ext 프로필을 따로 땄었다.
        // development-ext 이면, security rule 풀어놓은 환경에 터널링하여 ncp 에 붙도록 했다. 개발편의를 위해...ㅎ
        if (env.acceptsProfiles(Profiles.of("default", "development", "development-ext"))) {
            setShowSql(true)
        }
        setDatabase(Database.MYSQL)
        // db02 는 setDatabase(Database.POSTGRESQL) 이다.
    }
}

```

application-xxx.yml 은 아래처럼 설정했다.

```yaml
spring:
  datasource:
    db01:
      // AWS ParamStore 에서 값들을 가져온다.
      jdbc-url: ${/db01/.../datasource/url}
      username: ${/db01/.../datasource/password}
      password: ${/db01/.../datasource/password}
      driver-class-name: com.mysql.cj.jdbc.Driver
    db02:
      jdbc-url: ${/db02/.../datasource/url}
      username: ${/db02/.../datasource/username}
      password: ${/db02/.../datasource/password}
      driver-class-name: org.postgresql.Driver

  // 몽고 디비도 써야했다.
  data:
    mongodb:
      uri: ${...}

```

### QueryDSL 설정 코드

```kotlin
@Configuration
class QdslConfig {
    @PersistenceContext(unitName = Db01DbProps.persistenceUnit)
    private lateinit var db01EntityManager: EntityManager
    
    @PersistenceContext(unitName = Db02DbProps.persistenceUnit)
    private lateinit var db02EntityManager: EntityManager


    @Bean
    fun db02JpaQueryFactory() = Db02JpaQueryFactory(Db02EntityManager)

    @Bean
    fun db01JpaQueryFactory() = Db01JpaQueryFactory(Db01EntityManager)
}

// Qualifier 가 아닌 클래스명 만으로 빈을 지정하고 싶어 따로 정의하였다.
// JPAQueryFactory 가 미지원하는 insert 수행을 위해 em 을 별도 프로퍼티로 정의했다.
//  - private 로 만들고, 메소드를 새로이 여는게 맞아보이나, 쓰임새를 보고 일괄 리팩토링키로 하였다.
class Db01JpaQueryFactory(val entityManager: EntityManager) : JPAQueryFactory(entityManager)
class Db02JpaQueryFactory(val entityManager: EntityManager) : JPAQueryFactory(entityManager)

// 서비스에서 엔티티 패스를 짧게 쓰기위해 alias 를 정의했다.
object Db02QdslAlias {
    ...
}

object Db01QdslAlias {
    ...
}
```

리팩토링 여지는 남아있으나, 1차적으로 코드를 픽스하고 리뷰를 진행하였다.