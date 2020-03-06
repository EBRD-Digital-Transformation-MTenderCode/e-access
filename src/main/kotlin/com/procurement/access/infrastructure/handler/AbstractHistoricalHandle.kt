package com.procurement.access.infrastructure.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Action
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getVersion
import com.procurement.access.utils.tryToObject
import org.slf4j.LoggerFactory

abstract class AbstractHistoricalHandler<ACTION : Action, R : Any>(
    private val target: Class<ApiSuccessResponse>,
    private val historyRepository: HistoryDao
) : AbstractHandler<ACTION, ApiResponse>() {
    companion object {
        private val log = LoggerFactory.getLogger(AbstractHistoricalHandler::class.java)
    }

    override fun handle(node: JsonNode): ApiResponse {

        val id = node.getId().get
        val version = node.getVersion().get

        val history = historyRepository.getHistory(id.toString(), action.key)
        if (history != null) {
            val data = history.jsonData
            val result = data.tryToObject(target)
                .doOnError {
                    return responseError(
                        id = id,
                        version = version,
                        fails = listOf(
                            Fail.Incident.Parsing(data)
                        )
                    )
                }
            return result.get
        }
        val serviceResult = execute(node)
            .also {
                log.debug("'{}' has been executed. Result: '{}'", action.key, it)
            }

        return when (serviceResult) {
            is Result.Success -> ApiSuccessResponse(id = id, version = version, result = serviceResult.get)
                .also {
                    historyRepository.saveHistory(id.toString(), action.key, it)
                }
            is Result.Failure -> responseError(id = id, version = version, fails = serviceResult.error)
        }
    }

    abstract fun execute(node: JsonNode): Result<R, List<Fail>>
}
