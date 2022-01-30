object Db01Props {
    private const val identifier = "db01"

    const val configPropsPrefix = "spring.datasource.$identifier"

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
        if (env.acceptsProfiles(Profiles.of("default", "development", "development-ext"))) {
            setShowSql(true)
        }

        setDatabase(Database.MYSQL)
    }
}
