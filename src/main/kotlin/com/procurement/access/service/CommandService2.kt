package com.procurement.access.service

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.infrastructure.handler.get.lotids.GetLotIdsHandler
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.getAction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CommandService2(
    private val getLotIdsHandler: GetLotIdsHandler
) {
    companion object {
        private val log = LoggerFactory.getLogger(CommandService2::class.java)
    }

    fun execute(request: JsonNode): ApiResponse {

        val response = when (request.getAction().get) {
            Command2Type.GET_LOT_IDS -> {
                getLotIdsHandler.handle(node = request)
            }
        }

        if (log.isDebugEnabled)
            log.debug("DataOfResponse: '$response'.")
        return response
    }
}
