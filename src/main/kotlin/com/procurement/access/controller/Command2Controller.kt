package com.procurement.access.controller

import com.procurement.access.config.GlobalProperties
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.model.dto.bpe.Command2Message
import com.procurement.access.model.dto.bpe.errorResponse
import com.procurement.access.service.CommandService2
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
@RequestMapping("/command2")
class Command2Controller(private val commandService2: CommandService2) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(Command2Controller::class.java)
    }

    @PostMapping
    fun command(
        @RequestBody requestBody: String
    ): ResponseEntity<ApiResponse> {
        if (log.isDebugEnabled)
            log.debug("RECEIVED COMMAND: '$requestBody'.")

        val c2m: Command2Message = try {
            toObject(Command2Message::class.java, requestBody)
        } catch (expected: Exception) {
            log.debug("Error.", expected)
            val response =
                errorResponse(
                    exception = expected,
                    id = "N/A",
                    version = GlobalProperties.App.apiVersion
                )
            return ResponseEntity(response, HttpStatus.OK)
        }


        val response = try {
            commandService2.execute(c2m)
                .also { response ->
                    if (log.isDebugEnabled)
                        log.debug("RESPONSE (id: '${c2m.id}'): '${toJson(response)}'.")
                }
        } catch (expected: Exception) {
            log.debug("Error.", expected)
            errorResponse(
                exception = expected,
                id = c2m.id,
                version = c2m.version
            )
        }

        return ResponseEntity(response, HttpStatus.OK)
    }
}
