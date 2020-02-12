package com.procurement.access.service

import com.procurement.access.dao.HistoryDao
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.dto.lot.GetLotIdsRequest
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.model.dto.bpe.Command2Message
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CommandService2(
    private val historyDao: HistoryDao,
    private val lotsService: LotsService
) {
    companion object {
        private val log = LoggerFactory.getLogger(CommandService2::class.java)
    }

    fun execute(c2m: Command2Message): ApiSuccessResponse {
        val historyEntity = historyDao.getHistory(c2m.id.toString(), c2m.action.value())
        if (historyEntity != null) {
            return toObject(ApiSuccessResponse::class.java, historyEntity.jsonData)
        }

        val dataOfResponse = when (c2m.action) {
            Command2Type.GET_LOT_IDS -> {
                val request = toObject(GetLotIdsRequest::class.java, c2m.params)
                val result = lotsService.getLotIds(data = request.convert())
                val response = result.lotIds.toList()
                response.also {
                    log.debug("GET_LOT_IDS. Response: ${toJson(it)}")
                }
            }
        }
        val response = ApiSuccessResponse(
            version = c2m.version,
            id = c2m.id,
            result = dataOfResponse
        )
        historyDao.saveHistory(operationId = c2m.id.toString(), command = c2m.action.value(), response = response)
        return response
    }
}
