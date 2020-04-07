package com.procurement.access.application.service.tender

import com.procurement.access.application.model.params.SetStateForTenderParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.tender.strategy.get.awardCriteria.GetAwardCriteriaContext
import com.procurement.access.application.service.tender.strategy.get.awardCriteria.GetAwardCriteriaResult
import com.procurement.access.application.service.tender.strategy.get.awardCriteria.GetAwardCriteriaStrategy
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationContext
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationData
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationStrategy
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PreparedCancellationData
import com.procurement.access.application.service.tender.strategy.set.statefortender.SetStateForTenderStrategy
import com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful.SetTenderUnsuccessfulContext
import com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful.SetTenderUnsuccessfulResult
import com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful.SetTenderUnsuccessfulStrategy
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.handler.set.statefortender.SetStateForTenderResult
import org.springframework.stereotype.Service

interface ExtendTenderService {
    fun prepareCancellation(
        context: PrepareCancellationContext,
        data: PrepareCancellationData
    ): PreparedCancellationData

    fun getAwardCriteria(context: GetAwardCriteriaContext): GetAwardCriteriaResult

    fun setTenderUnsuccessful(context: SetTenderUnsuccessfulContext): SetTenderUnsuccessfulResult

    fun setStateForTender(params: SetStateForTenderParams): Result<SetStateForTenderResult, Fail>
}

@Service
class ExtendTenderServiceImpl(
    tenderProcessDao: TenderProcessDao,
    tenderProcessRepository: TenderProcessRepository
) : ExtendTenderService {

    private val prepareCancellationStrategy = PrepareCancellationStrategy(tenderProcessDao)
    private val getAwardCriteriaStrategy = GetAwardCriteriaStrategy(tenderProcessDao)
    private val setTenderUnsuccessfulStrategy = SetTenderUnsuccessfulStrategy(tenderProcessDao)
    private val setStateForTenderStrategy = SetStateForTenderStrategy(tenderProcessRepository)

    override fun prepareCancellation(
        context: PrepareCancellationContext,
        data: PrepareCancellationData
    ): PreparedCancellationData = prepareCancellationStrategy.execute(context = context, data = data)

    override fun getAwardCriteria(context: GetAwardCriteriaContext) = getAwardCriteriaStrategy.execute(context)

    override fun setTenderUnsuccessful(context: SetTenderUnsuccessfulContext) =
        setTenderUnsuccessfulStrategy.execute(context)

    override fun setStateForTender(params: SetStateForTenderParams): Result<SetStateForTenderResult, Fail> =
        setStateForTenderStrategy.execute(params = params)

}
