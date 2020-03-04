package com.procurement.access.infrastructure.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Action
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getVersion
import org.slf4j.LoggerFactory

abstract class AbstractValidationHandler<ACTION : Action> : AbstractHandler<ACTION, ApiResponse>() {
    companion object {
        private val log = LoggerFactory.getLogger(AbstractValidationHandler::class.java)
    }

    override fun handle(node: JsonNode): ApiResponse {
        val id = node.getId().get
        val version = node.getVersion().get

        val result = execute(node)
            .also {
                log.debug("'{}' has been executed. Result: '{}'", action.value, it)
            }

        return when (result) {
            is ValidationResult.Ok -> ApiSuccessResponse(version = version, id = id)
            is ValidationResult.Fail -> responseError(id = id, version = version, fails = result.error)
        }
    }

    abstract fun execute(node: JsonNode): ValidationResult<List<Fail>>
}