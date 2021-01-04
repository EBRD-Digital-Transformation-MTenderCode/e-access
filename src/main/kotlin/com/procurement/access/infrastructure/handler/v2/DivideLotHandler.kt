package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepository
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.DivideLotRequest
import com.procurement.access.infrastructure.handler.v2.model.response.DivideLotResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.LotsService
import org.springframework.stereotype.Service

@Service
class DivideLotHandler(
    private val lotsService: LotsService,
    transform: Transform,
    historyRepository: HistoryRepository,
    logger: Logger
) : AbstractHistoricalHandler<DivideLotResult>(transform, historyRepository, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.DIVIDE_LOT

    override fun execute(descriptor: CommandDescriptor): Result<DivideLotResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<DivideLotRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return lotsService.divideLot(params = params)
    }
}
