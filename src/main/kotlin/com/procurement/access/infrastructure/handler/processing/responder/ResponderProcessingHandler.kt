package com.procurement.access.infrastructure.handler.processing.responder

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.model.dto.bpe.CommandTypeV2
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.ResponderService
import org.springframework.stereotype.Service

@Service
class ResponderProcessingHandler(
    private val responderService: ResponderService,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<CommandTypeV2, ResponderProcessingResult>(
    historyRepository = historyDao,
    target = ApiResponseV2.Success::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<ResponderProcessingResult, Fail> {
        val params = node.tryGetParams()
            .flatMap { it.tryParamsToObject(ResponderProcessingRequest.Params::class.java) }
            .flatMap { it.convert() }
            .onFailure { error -> return error }

        return responderService.responderProcessing(params = params)
    }

    override val action: CommandTypeV2
        get() = CommandTypeV2.RESPONDER_PROCESSING
}
