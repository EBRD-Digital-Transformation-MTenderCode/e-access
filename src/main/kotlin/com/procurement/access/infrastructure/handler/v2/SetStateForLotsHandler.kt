package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.application.service.lot.LotService
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepositoryNew
import com.procurement.access.infrastructure.handler.HistoryRepositoryOld
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.SetStateForLotsRequest
import com.procurement.access.infrastructure.handler.v2.model.response.SetStateForLotsResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import org.springframework.stereotype.Service

@Service
class SetStateForLotsHandler(
    private val lotService: LotService,
    transform: Transform,
    historyRepositoryOld: HistoryRepositoryOld,
    historyRepositoryNew: HistoryRepositoryNew,
    logger: Logger
) : AbstractHistoricalHandler<List<SetStateForLotsResult>>(
    transform,
    historyRepositoryOld,
    historyRepositoryNew,
    logger
) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.SET_STATE_FOR_LOTS

    override fun execute(descriptor: CommandDescriptor): Result<List<SetStateForLotsResult>, Fail> {
        val params = descriptor.body.asJsonNode
            .params<SetStateForLotsRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return lotService.setStateForLots(params = params)
    }
}
