package com.procurement.access.infrastructure.handler.processing.responder

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.model.responder.processing.ResponderProcessingParams
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.BadRequestErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.service.ResponderService
import com.procurement.access.utils.getStageFromOcid
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

@Service
class ResponderProcessingHandler(
    private val responderService: ResponderService,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<Command2Type, ResponderProcessingResponse>(
    historyRepository = historyDao,
    target = ApiSuccessResponse::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<ResponderProcessingResponse, Fail> {
        val paramsNode = node.tryGetParams()
            .doOnError { error -> return Result.failure(error) }
            .get

        val params: ResponderProcessingParams = paramsNode.tryToObject(ResponderProcessingRequest::class.java)
            .doOnError { error ->
                return Result.failure(
                    BadRequestErrors.Parsing(
                        message = "Can not parse to ${error.className}",
                        request = paramsNode.toString()
                    )
                )
            }
            .get
            .convert()
            .doOnError { error -> return Result.failure(error) }
            .get

        val stage = params.ocid.getStageFromOcid()

        return responderService.responderProcessing(params = params, stage = stage)
    }

    override val action: Command2Type
        get() = Command2Type.RESPONDER_PROCESSING
}
