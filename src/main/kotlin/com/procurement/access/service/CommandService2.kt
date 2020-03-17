package com.procurement.access.service

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.infrastructure.handler.check.accesstotender.CheckAccessToTenderHandler
import com.procurement.access.infrastructure.handler.get.lotStateByIds.GetLotStateByIdsHandler
import com.procurement.access.infrastructure.handler.get.lotids.GetLotIdsHandler
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.errorResponse
import com.procurement.access.model.dto.bpe.getAction
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getVersion
import org.springframework.stereotype.Service

@Service
class CommandService2(
    private val getLotIdsHandler: GetLotIdsHandler,
    private val checkAccessToTenderHandler: CheckAccessToTenderHandler,
    private val getLotStateByIdsHandler: GetLotStateByIdsHandler,
    private val logger: Logger
) {

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
            Command2Type.CHECK_ACCESS_TO_TENDER -> checkAccessToTenderHandler.handle(node = request)
            Command2Type.GET_LOT_STATE_BY_IDS -> getLotStateByIdsHandler.handle(node = request)
        }

        logger.info("DataOfResponse: '$response'.")
        return response
    }
}
