package com.procurement.access.infrastructure.web.controller

import com.procurement.access.exception.EnumElementProviderException
import com.procurement.access.exception.ErrorException
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.ResponseDto
import com.procurement.access.infrastructure.api.v1.getEnumExceptionResponseDto
import com.procurement.access.infrastructure.api.v1.getErrorExceptionResponseDto
import com.procurement.access.infrastructure.api.v1.getExceptionResponseDto
import com.procurement.access.service.CommandService
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/command")
class CommandControllerV1(private val commandService: CommandService) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(CommandControllerV1::class.java)
    }

    @PostMapping
    fun command(@RequestBody requestBody: String): ResponseEntity<ResponseDto> {
        if (log.isDebugEnabled)
            log.debug("RECEIVED COMMAND: '$requestBody'.")
        val cm: CommandMessage = toObject(CommandMessage::class.java, requestBody)

        val response = commandService.execute(cm)
        if (log.isDebugEnabled)
            log.debug("RESPONSE (operation-id: '${cm.context.operationId}'): '${toJson(response)}'.")
        return ResponseEntity(response, HttpStatus.OK)
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception::class)
    fun exception(ex: Exception): ResponseDto {
        log.error("Error.", ex)
        return when (ex) {
            is ErrorException -> getErrorExceptionResponseDto(ex)
            is EnumElementProviderException -> getEnumExceptionResponseDto(ex)
            else -> getExceptionResponseDto(ex)
        }
    }
}
