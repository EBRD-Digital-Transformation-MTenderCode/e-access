package com.procurement.access.infrastructure.handler.calculate.value

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.bind
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
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
    target = ApiSuccessResponse::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<CalculateAPValueResult, Fail> {
        val params = node.tryGetParams()
            .bind { it.tryParamsToObject(CalculateAPValueRequest::class.java) }
            .bind { it.convert() }
            .orForwardFail { error -> return error }

        return apService.calculateAPValue(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.CALCULATE_AP_VALUE
}
