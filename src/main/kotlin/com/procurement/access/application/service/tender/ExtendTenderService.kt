package com.procurement.access.application.service.tender

import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationContext
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationData
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationStrategy
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PreparedCancellationData
import com.procurement.access.dao.TenderProcessDao
import org.springframework.stereotype.Service

interface ExtendTenderService {
    fun prepareCancellation(
        context: PrepareCancellationContext,
        data: PrepareCancellationData
    ): PreparedCancellationData
}

@Service
class ExtendTenderServiceImpl(
    tenderProcessDao: TenderProcessDao
) : ExtendTenderService {

    private val prepareCancellationStrategy = PrepareCancellationStrategy(tenderProcessDao)

    override fun prepareCancellation(
        context: PrepareCancellationContext,
        data: PrepareCancellationData
    ): PreparedCancellationData = prepareCancellationStrategy.execute(context = context, data = data)
}
