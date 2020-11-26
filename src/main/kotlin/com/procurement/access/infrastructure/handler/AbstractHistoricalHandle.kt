package com.procurement.access.infrastructure.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Action
import com.procurement.access.infrastructure.web.dto.ApiResponseV2
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.lib.functional.Result
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getVersion
import com.procurement.access.utils.toJson
import com.procurement.access.utils.tryToObject

abstract class AbstractHistoricalHandler<ACTION : Action, R : Any>(
    private val target: Class<ApiSuccessResponse>,
    private val historyRepository: HistoryDao,
    private val logger: Logger
) : AbstractHandler<ACTION, ApiResponseV2>(logger = logger) {


    override fun handle(node: JsonNode): ApiResponseV2 {

        val id = node.getId().get
        val version = node.getVersion().get

        val history = historyRepository.getHistory(id.toString(), action.key)
        if (history != null) {
            val data = history.jsonData
            return data.tryToObject(target)
                .onFailure {
                    return responseError(
                        id = id,
                        version = version,
                        fail = Fail.Incident.ParsingIncident()
                    )
                }
        }

        return when (val serviceResult = execute(node)) {
            is Result.Success -> ApiSuccessResponse(id = id, version = version, result = serviceResult.get)
                .also {
                    logger.info("'${action.key}' has been executed. Result: '${toJson(it)}'")
                    historyRepository.saveHistory(id.toString(), action.key, it)
                }
            is Result.Failure -> responseError(id = id, version = version, fail = serviceResult.reason)
        }
    }

    abstract fun execute(node: JsonNode): Result<R, Fail>
}
