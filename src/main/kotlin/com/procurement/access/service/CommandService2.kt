package com.procurement.access.service

import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.model.enums.ResponseStatus
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.dto.lot.GetLotIdsByStatesRequest
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse2
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

    fun execute(c2m: Command2Message): ApiSuccessResponse2 {
        val historyEntity = historyDao.getHistory(c2m.id, c2m.action.value())
        if (historyEntity != null) {
            return toObject(ApiSuccessResponse2::class.java, historyEntity.jsonData)
        }

        val dataOfResponse = when (c2m.action) {
            Command2Type.GET_LOT_IDS_BY_STATES -> {
                val request = toObject(GetLotIdsByStatesRequest::class.java, c2m.params)
                val result = lotsService.getLotIdsByStates(data = request.convert())
                val response = result.convert()
                response.also {
                    log.debug("GET_LOT_IDS_BY_STATES. Response: ${toJson(it)}")
                }
            }
        }
        val response = ApiSuccessResponse2(
            version = c2m.version,
            id = c2m.id,
            status = ResponseStatus.SUCCESS,
            result = dataOfResponse
        )
        historyDao.saveHistory(operationId = c2m.id, command = c2m.action.value(), response = response)
        return response
    }
}
