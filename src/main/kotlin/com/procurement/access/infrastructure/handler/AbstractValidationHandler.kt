package com.procurement.access.infrastructure.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Action
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getVersion
import com.procurement.access.utils.toJson

abstract class AbstractValidationHandler<ACTION : Action>(
    private val logger: Logger
) : AbstractHandler<ACTION, ApiResponse>(logger = logger) {

    override fun handle(node: JsonNode): ApiResponse {
        val id = node.getId().get
        val version = node.getVersion().get

        return when (val result = execute(node)) {
            is ValidationResult.Ok -> ApiSuccessResponse(version = version, id = id)
                .also {
                    logger.info("'${action.key}' has been executed. Result: '${toJson(it)}'")
                }
            is ValidationResult.Fail -> responseError(id = id, version = version, fail = result.error)
        }
    }

    abstract fun execute(node: JsonNode): ValidationResult<Fail>
}