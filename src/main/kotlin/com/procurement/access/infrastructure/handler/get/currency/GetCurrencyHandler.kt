package com.procurement.access.infrastructure.handler.get.currency

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.dto.converter.get.currency.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.lib.functional.Result
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.TenderService
import org.springframework.stereotype.Service

@Service
class GetCurrencyHandler(
    private val tenderService: TenderService,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<Command2Type, GetCurrencyResult>(
    historyRepository = historyDao,
    target = ApiSuccessResponse::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<GetCurrencyResult, Fail> {

        val paramsNode = node.tryGetParams()
            .onFailure { error -> return error }

        val params = paramsNode.tryParamsToObject(GetCurrencyRequest::class.java)
            .onFailure { error -> return error }
            .convert()
            .onFailure { error -> return error }

        return tenderService.getCurrency(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.GET_QUALIFICATION_CRITERIA_AND_METHOD
}
