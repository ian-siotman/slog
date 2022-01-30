object Db02Props {
    private const val identifier = "db02"
    const val configPropsPrefix = "spring.datasource.$identifier"

    const val basePackage = "kr.co.db02.domain.$identifier"
    const val dataSource = "${identifier}DataSource"
    const val persistenceUnit = "${identifier}PersistenceUnit"
    const val transactionManager = "${identifier}TransactionManager"
    const val entityManagerFactory = "${identifier}EntityManagerFactory"
}

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = [Db02Props.basePackage],
    transactionManagerRef = Db02Props.transactionManager,
    entityManagerFactoryRef = Db02Props.entityManagerFactory
)
class Db02DataSourceConfig(private val env: Environment) {
    @Bean(name = [Db02Props.dataSource])
    @ConfigurationProperties(prefix = Db02Props.configPropsPrefix)
    fun dataSource(): DataSource = DataSourceBuilder.create().build()

    @Bean(name = [Db02Props.entityManagerFactory])
    fun entityManagerFactory() = LocalContainerEntityManagerFactoryBean().apply {
        this.dataSource = dataSource()
        this.jpaVendorAdapter = jpaVendorAdapter()
        this.persistenceUnitName = Db02Props.persistenceUnit

        this.setPackagesToScan(Db02Props.basePackage)
    }

    @Bean(name = [Db02Props.transactionManager])
    fun transactionManager() = JpaTransactionManager().apply {
        this.entityManagerFactory = entityManagerFactory().`object`
    }

    private fun jpaVendorAdapter() = HibernateJpaVendorAdapter().apply {
        if (env.acceptsProfiles(Profiles.of("default", "development", "development-ext"))) {
            setShowSql(true)
        }

        setDatabase(Database.POSTGRESQL)
    }
}
