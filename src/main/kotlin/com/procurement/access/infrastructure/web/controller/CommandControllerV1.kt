package com.procurement.access.infrastructure.web.controller

import com.procurement.access.exception.EnumElementProviderException
import com.procurement.access.exception.ErrorException
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId
import com.procurement.access.infrastructure.api.v1.ApiResponseV1
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.businessError
import com.procurement.access.infrastructure.api.v1.commandId
import com.procurement.access.infrastructure.api.v1.internalServerError
import com.procurement.access.service.CommandServiceV1
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/command")
class CommandControllerV1(
    private val commandService: CommandServiceV1,
    private val logger: com.procurement.access.application.service.Logger
) {

    @PostMapping
    fun command(@RequestBody requestBody: String): ResponseEntity<ApiResponseV1> {
        if (logger.isDebugEnabled)
            logger.debug("RECEIVED COMMAND: '$requestBody'.")
        val cm: CommandMessage = try {
            toObject(CommandMessage::class.java, requestBody)
        } catch (expected: Exception) {
            val response = errorResponse(exception = expected, id = CommandId.NaN, version = ApiVersion.NaN)
            return ResponseEntity(response, HttpStatus.OK)
        }

        val response = try {
            commandService.execute(cm)
                .also { response ->
                    if (logger.isDebugEnabled)
                        logger.debug("RESPONSE (operation-id: '${cm.context.operationId}'): '${toJson(response)}'.")
                }
        } catch (expected: Exception) {
            errorResponse(exception = expected, id = cm.commandId, version = cm.version)
        }

        return ResponseEntity(response, HttpStatus.OK)
    }

    fun errorResponse(
        version: ApiVersion,
        id: CommandId,
        exception: Exception
    ): ApiResponseV1.Failure {
        logger.error(message = "Error.", exception = exception)
        return when (exception) {
            is ErrorException -> ApiResponseV1.Failure.businessError(
                version = version,
                id = id,
                code = exception.code,
                description = exception.message ?: exception.toString()
            )

            is EnumElementProviderException -> ApiResponseV1.Failure.businessError(
                version = version,
                id = id,
                code = exception.code,
                description = exception.message ?: exception.toString()
            )

            else -> ApiResponseV1.Failure.internalServerError(
                version = version,
                id = id,
                description = exception.message ?: exception.toString()
            )
        }
    }
}
