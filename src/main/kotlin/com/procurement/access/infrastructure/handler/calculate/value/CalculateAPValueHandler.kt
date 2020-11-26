package com.procurement.access.infrastructure.handler.calculate.value

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.APService
import org.springframework.stereotype.Service

@Service
class CalculateAPValueHandler(
    private val apService: APService,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<Command2Type, CalculateAPValueResult>(
    historyRepository = historyDao,
    target = ApiResponseV2.Success::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<CalculateAPValueResult, Fail> {
        val params = node.tryGetParams()
            .flatMap { it.tryParamsToObject(CalculateAPValueRequest::class.java) }
            .flatMap { it.convert() }
            .onFailure { error -> return error }

        return apService.calculateAPValue(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.CALCULATE_AP_VALUE
}
