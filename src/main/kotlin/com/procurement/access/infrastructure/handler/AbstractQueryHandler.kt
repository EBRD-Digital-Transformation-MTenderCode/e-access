package com.procurement.access.infrastructure.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Action
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getVersion
import com.procurement.access.utils.toJson

abstract class AbstractQueryHandler<ACTION : Action, R : Any?>
    (
    private val logger: Logger
) : AbstractHandler<ACTION, ApiResponse>(logger = logger) {

    override fun handle(node: JsonNode): ApiResponse {
        val id = node.getId().get
        val version = node.getVersion().get

        return when (val result = execute(node)) {
            is Result.Success -> {
                if (logger.isDebugEnabled)
                    logger.debug("${action.key} has been executed. Result: ${toJson(result.get)}")
                return ApiSuccessResponse(version = version, id = id, result = result.get)
            }
            is Result.Failure -> responseError(
                fail = result.error, version = version, id = id
            )
        }
    }

    abstract fun execute(node: JsonNode): Result<R, Fail>
}