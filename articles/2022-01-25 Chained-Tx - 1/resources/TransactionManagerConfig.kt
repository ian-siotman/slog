import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.transaction.ChainedTransactionManager
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class TransactionManagerConfig {
    companion object {
        const val chainedTxManagerQualifier = "chainedTransactionManager"
    }

    @Bean
    fun chainedTransactionManager(
        @Qualifier(Db01Props.transactionManager) db01TransactionManager: PlatformTransactionManager,
        @Qualifier(Db02Props.transactionManager) db02TransactionManager: PlatformTransactionManager
    ) = ChainedTransactionManager(db01TransactionManager, db02TransactionManager)
}
