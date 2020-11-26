package com.procurement.access.infrastructure.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Action
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getVersion
import com.procurement.access.utils.toJson

abstract class AbstractValidationHandler<ACTION : Action>(
    private val logger: Logger
) : AbstractHandler<ACTION, ApiResponseV2>(logger = logger) {

    override fun handle(node: JsonNode): ApiResponseV2 {
        val id = node.getId().get
        val version = node.getVersion().get

        return when (val result = execute(node)) {
            is ValidationResult.Ok -> ApiResponseV2.Success(version = version, id = id)
                .also {
                    logger.info("'${action.key}' has been executed. Result: '${toJson(it)}'")
                }
            is ValidationResult.Error -> responseError(id = id, version = version, fail = result.reason)
        }
    }

    abstract fun execute(node: JsonNode): ValidationResult<Fail>
}