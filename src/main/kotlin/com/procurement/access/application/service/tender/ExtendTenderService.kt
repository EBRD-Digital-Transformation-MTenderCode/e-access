package com.procurement.access.application.service.tender

import com.procurement.access.application.service.tender.strategy.get.awardCriteria.GetAwardCriteriaContext
import com.procurement.access.application.service.tender.strategy.get.awardCriteria.GetAwardCriteriaResult
import com.procurement.access.application.service.tender.strategy.get.awardCriteria.GetAwardCriteriaStrategy
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationContext
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationData
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationStrategy
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PreparedCancellationData
import com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful.SetTenderUnsuccessfulContext
import com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful.SetTenderUnsuccessfulResult
import com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful.SetTenderUnsuccessfulStrategy
import com.procurement.access.dao.TenderProcessDao
import org.springframework.stereotype.Service

interface ExtendTenderService {
    fun prepareCancellation(
        context: PrepareCancellationContext,
        data: PrepareCancellationData
    ): PreparedCancellationData

    fun getAwardCriteria(context: GetAwardCriteriaContext): GetAwardCriteriaResult

    fun setTenderUnsuccessful(context: SetTenderUnsuccessfulContext): SetTenderUnsuccessfulResult
}

@Service
class ExtendTenderServiceImpl(
    tenderProcessDao: TenderProcessDao
) : ExtendTenderService {

    private val prepareCancellationStrategy = PrepareCancellationStrategy(tenderProcessDao)
    private val getAwardCriteriaStrategy = GetAwardCriteriaStrategy(tenderProcessDao)
    private val setTenderUnsuccessfulStrategy = SetTenderUnsuccessfulStrategy(tenderProcessDao)

    override fun prepareCancellation(
        context: PrepareCancellationContext,
        data: PrepareCancellationData
    ): PreparedCancellationData = prepareCancellationStrategy.execute(context = context, data = data)

    override fun getAwardCriteria(context: GetAwardCriteriaContext) = getAwardCriteriaStrategy.execute(context)

    override fun setTenderUnsuccessful(context: SetTenderUnsuccessfulContext) =
        setTenderUnsuccessfulStrategy.execute(context)
}
