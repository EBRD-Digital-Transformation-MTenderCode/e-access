package com.procurement.access.service

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.dao.HistoryDao
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.getAction
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.service.handler.GetLotIdsHandler
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CommandService2(
    private val historyDao: HistoryDao,
    private val getLotIdsHandler: GetLotIdsHandler
) {
    companion object {
        private val log = LoggerFactory.getLogger(CommandService2::class.java)
    }

    fun execute(request: JsonNode): ApiSuccessResponse {

        val id = request.getId()
        val action = request.getAction()

        val historyEntity = historyDao.getHistory(id.toString(), action.value())
        if (historyEntity != null) {
            return toObject(ApiSuccessResponse::class.java, historyEntity.jsonData)
        }

        val response: ApiSuccessResponse = when (action) {
            Command2Type.GET_LOT_IDS -> {

                val response = getLotIdsHandler.handle(request = request)
                response.also {
                    log.debug("GET_LOT_IDS. Response: ${toJson(it)}")
                }
            }
        }

        historyDao.saveHistory(operationId = id.toString(), command = action.value(), response = response)
        return response
    }
}
