package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.CalculateAPValueRequest
import com.procurement.access.infrastructure.handler.v2.model.response.CalculateAPValueResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.APService
import org.springframework.stereotype.Service

@Service
class CalculateAPValueHandler(
    private val apService: APService,
    transform: Transform,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<CalculateAPValueResult>(transform, historyDao, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.CALCULATE_AP_VALUE

    override fun execute(descriptor: CommandDescriptor): Result<CalculateAPValueResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<CalculateAPValueRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }

        return apService.calculateAPValue(params = params)
    }
}
