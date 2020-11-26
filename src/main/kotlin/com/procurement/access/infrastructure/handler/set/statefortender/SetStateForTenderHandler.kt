package com.procurement.access.infrastructure.handler.set.statefortender

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.tender.ExtendTenderService
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.lib.functional.Result
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import org.springframework.stereotype.Service

@Service
class SetStateForTenderHandler(
    private val tenderService: ExtendTenderService,
    private val historyDao: HistoryDao,
    private val logger: Logger
) : AbstractHistoricalHandler<Command2Type, SetStateForTenderResult>(
    historyRepository = historyDao,
    target = ApiSuccessResponse::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<SetStateForTenderResult, Fail> {

        val paramsNode = node.tryGetParams()
            .doOnError { error -> return Result.failure(error) }
            .get

        val params = paramsNode.tryParamsToObject(SetStateForTenderRequest::class.java)
            .doOnError { error -> return Result.failure(error) }
            .get
            .convert()
            .doOnError { error -> return Result.failure(error) }
            .get

        return tenderService.setStateForTender(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.SET_STATE_FOR_TENDER
}
