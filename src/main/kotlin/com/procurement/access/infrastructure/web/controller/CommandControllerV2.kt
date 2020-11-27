package com.procurement.access.infrastructure.web.controller

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.BadRequest
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.api.v2.errorResponse
import com.procurement.access.infrastructure.extension.tryGetAttributeAsEnum
import com.procurement.access.infrastructure.extension.tryGetTextAttribute
import com.procurement.access.infrastructure.handler.v2.CommandDescriptor
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.CommandServiceV2
import com.procurement.access.utils.toJson
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/command2")
class CommandControllerV2(
    private val commandService2: CommandServiceV2,
    private val transform: Transform,
    private val logger: Logger
) {

    @PostMapping
    fun command(
        @RequestBody requestBody: String
    ): ResponseEntity<ApiResponseV2> {

        logger.info("RECEIVED COMMAND: '${requestBody}'.")
        val node = requestBody.tryGetNode(transform)
            .onFailure { return responseEntity(fail = it.reason, id = CommandId.NaN, version = ApiVersion.NaN) }

        val version = node.tryGetVersion()
            .onFailure {
                val id = node.tryGetId().getOrElse(CommandId.NaN)
                return responseEntity(fail = it.reason, version = ApiVersion.NaN, id = id)
            }

        val id = node.tryGetId()
            .onFailure { return responseEntity(fail = it.reason, version = version, id = CommandId.NaN) }

        val action = node.tryGetAction()
            .onFailure { return responseEntity(fail = it.reason, version = version, id = id) }

        val description = CommandDescriptor(
            version = version,
            id = id,
            action = action,
            body = CommandDescriptor.Body(asString = requestBody, asJsonNode = node)
        )

        val response =
            commandService2.execute(description)
                .also { response ->
                    if (logger.isDebugEnabled)
                        logger.debug("RESPONSE (id: '${id}'): '${toJson(response)}'.")
                }

        return ResponseEntity(response, HttpStatus.OK)
    }

    fun String.tryGetNode(transform: Transform): Result<JsonNode, BadRequest> =
        when (val result = transform.tryParse(this)) {
            is Result.Success -> result
            is Result.Failure -> failure(BadRequest(description = "Error parsing payload.", exception = result.reason.exception))
        }

    fun JsonNode.tryGetVersion(): Result<ApiVersion, DataErrors> {
        val name = "version"
        return tryGetTextAttribute(name)
            .flatMap { version ->
                ApiVersion.orNull(version)
                    ?.asSuccess<ApiVersion, DataErrors>()
                    ?: DataErrors.Validation.DataFormatMismatch(
                        name = name,
                        expectedFormat = ApiVersion.pattern,
                        actualValue = version
                    ).asFailure()
            }
    }

    fun JsonNode.tryGetAction(): Result<CommandTypeV2, DataErrors> = tryGetAttributeAsEnum("action", CommandTypeV2)

    fun JsonNode.tryGetId(): Result<CommandId, DataErrors> = tryGetTextAttribute("id").map { CommandId(it) }

    private fun responseEntity(fail: Fail, id: CommandId, version: ApiVersion): ResponseEntity<ApiResponseV2> {
        fail.logging(logger)
        val response = errorResponse(fail = fail, id = id, version = version)
        return ResponseEntity(response, HttpStatus.OK)
    }
}
