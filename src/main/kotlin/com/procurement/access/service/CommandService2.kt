package com.procurement.access.service

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.infrastructure.handler.get.lotids.GetLotIdsHandler
import com.procurement.access.infrastructure.handler.processing.responder.ResponderProcessingHandler
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.errorResponse
import com.procurement.access.model.dto.bpe.getAction
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getVersion
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CommandService2(
    private val getLotIdsHandler: GetLotIdsHandler,
    private val responderProcessingHandler: ResponderProcessingHandler
) {
    companion object {
        private val log = LoggerFactory.getLogger(CommandService2::class.java)
    }

    fun execute(request: JsonNode): ApiResponse {

        val id = request.getId()
            .doOnError { error -> return errorResponse(fail = error) }
            .get

        val version = request.getVersion()
            .doOnError { error -> return errorResponse(id = id, fail = error) }
            .get

        val action = request.getAction()
            .doOnError { error -> return errorResponse(id = id, version = version, fail = error) }
            .get

        val response = when (action) {
            Command2Type.GET_LOT_IDS -> getLotIdsHandler.handle(node = request)
            Command2Type.RESPONDER_PROCESSING -> responderProcessingHandler.handle(node = request)
        }

        if (log.isDebugEnabled)
            log.debug("DataOfResponse: '$response'.")
        return response
    }
}
