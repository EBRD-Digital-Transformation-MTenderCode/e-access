package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepositoryNew
import com.procurement.access.infrastructure.handler.HistoryRepositoryOld
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.converter.convert
import com.procurement.access.infrastructure.handler.v2.model.request.GetBuyersOwnersRequest
import com.procurement.access.infrastructure.handler.v2.model.response.GetBuyersOwnersResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.TenderService
import org.springframework.stereotype.Service

@Service
class GetBuyersOwnersHandler(
    private val tenderService: TenderService,
    transform: Transform,
    historyRepositoryOld: HistoryRepositoryOld,
    historyRepositoryNew: HistoryRepositoryNew,
    logger: Logger
) : AbstractHistoricalHandler<GetBuyersOwnersResult>(transform, historyRepositoryOld, historyRepositoryNew, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.GET_BUYERS_OWNERS

    override fun execute(descriptor: CommandDescriptor): Result<GetBuyersOwnersResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<GetBuyersOwnersRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return tenderService.getBuyersOwners(params = params)
    }
}
