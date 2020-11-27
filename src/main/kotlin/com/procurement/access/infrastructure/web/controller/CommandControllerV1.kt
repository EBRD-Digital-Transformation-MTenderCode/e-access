package com.procurement.access.infrastructure.web.controller

import com.procurement.access.exception.EnumElementProviderException
import com.procurement.access.exception.ErrorException
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId
import com.procurement.access.infrastructure.api.v1.ApiResponseV1
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.commandId
import com.procurement.access.service.CommandService
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
class CommandControllerV1(private val commandService: CommandService) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(CommandControllerV1::class.java)
    }

    @PostMapping
    fun command(@RequestBody requestBody: String): ResponseEntity<ApiResponseV1> {
        if (log.isDebugEnabled)
            log.debug("RECEIVED COMMAND: '$requestBody'.")
        val cm: CommandMessage = try {
            toObject(CommandMessage::class.java, requestBody)
        } catch (expected: Exception) {
            val response = errorResponse(exception = expected, id = CommandId.NaN, version = ApiVersion.NaN)
            return ResponseEntity(response, HttpStatus.OK)
        }

        val response = try {
            commandService.execute(cm)
                .also { response ->
                    if (log.isDebugEnabled)
                        log.debug("RESPONSE (operation-id: '${cm.context.operationId}'): '${toJson(response)}'.")
                }
        } catch (expected: Exception) {
            log.debug("Error.", expected)
            errorResponse(exception = expected, id = cm.commandId, version = cm.version)
        }

        return ResponseEntity(response, HttpStatus.OK)
    }

    fun errorResponse(exception: Exception, id: CommandId, version: ApiVersion): ApiResponseV1.Failure {
        log.error("Error.", exception)
        return when (exception) {
            is ErrorException -> getErrorExceptionResponseDto(exception = exception, id = id, version = version)
            is EnumElementProviderException -> getEnumExceptionResponseDto(exception = exception, id = id, version = version)
            else -> getExceptionResponseDto(exception = exception, id = id, version = version)
        }
    }

    fun getExceptionResponseDto(exception: Exception, id: CommandId, version: ApiVersion): ApiResponseV1.Failure {
        return ApiResponseV1.Failure(
            version = version,
            id = id,
            errors = listOf(
                ApiResponseV1.Failure.Error(
                    code = "400.03.00",
                    description = exception.message ?: exception.toString()
                )
            )
        )
    }

    fun getErrorExceptionResponseDto(exception: ErrorException, id: CommandId, version: ApiVersion): ApiResponseV1.Failure {
        return ApiResponseV1.Failure(
            version = version,
            id = id,
            errors = listOf(
                ApiResponseV1.Failure.Error(
                    code = "400.03." + exception.error.code,
                    description = exception.message ?: exception.toString()
                )
            )
        )
    }

    fun getEnumExceptionResponseDto(exception: EnumElementProviderException, id: CommandId, version: ApiVersion): ApiResponseV1.Failure {
        return ApiResponseV1.Failure(
            id = id,
            version = version,
            errors = listOf(
                ApiResponseV1.Failure.Error(
                    code = "400.03." + exception.code,
                    description = exception.message ?: exception.toString()
                )
            )
        )
    }
}
