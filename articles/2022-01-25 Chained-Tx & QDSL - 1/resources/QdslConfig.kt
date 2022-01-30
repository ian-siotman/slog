
@Configuration
class QdslConfig {
    @PersistenceContext(unitName = Db02DbProps.persistenceUnit)
    private lateinit var Db02EntityManager: EntityManager

    @PersistenceContext(unitName = Db01DbProps.persistenceUnit)
    private lateinit var Db01EntityManager: EntityManager

    @Bean
    fun Db02JpaQueryFactory() = Db02JpaQueryFactory(Db02EntityManager)

    @Bean
    fun Db01JpaQueryFactory() = Db01JpaQueryFactory(Db01EntityManager)
}

class Db01JpaQueryFactory(val entityManager: EntityManager) : JPAQueryFactory(entityManager)
class Db02JpaQueryFactory(val entityManager: EntityManager) : JPAQueryFactory(entityManager)

object Db02QdslAlias {
    val qLms = QLoanMasterSchedule.loanMasterSchedule
    val qLmsOrigin = QLoanMasterScheduleOrigin.loanMasterScheduleOrigin
}

object Db01QdslAlias {
    val qContractSkd = QContractSkd.contractSkd
    val qTradeBook = QTradeBook.tradeBook
    val qInMoney = QInMoney.inMoney
    val qPreMoney = QPreMoney.preMoney
    val qContractInfo = QContractInfo.contractInfo

    val qRefundMoney = QRefundMoney.refundMoney
    val qRefundMoneyDetail = QRefundMoneyDetail.refundMoneyDetail
    val qTenderList = QTenderList.tenderList
    val qTenderListDetail = QTenderListDetail.tenderListDetail
}
