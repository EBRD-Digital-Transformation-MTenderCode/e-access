package com.procurement.access.controller

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.config.GlobalProperties
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.model.dto.bpe.Command2Message
import com.procurement.access.model.dto.bpe.NaN
import com.procurement.access.model.dto.bpe.errorResponse
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getVersion
import com.procurement.access.service.CommandService2
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toNode
import com.procurement.access.utils.toObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

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

        try {
            toObject(Command2Message::class.java, requestBody)
        } catch (expected: Exception) {
            log.debug("Error.", expected)
            return createErrorResponseEntity(
                expected = expected
            )
        }

        val node: JsonNode = try {
            requestBody.toNode()
        } catch (expected: Exception) {
            log.debug("Error.", expected)
            return createErrorResponseEntity(
                expected = expected
            )
        }

        val id = node.getId()
        val version = node.getVersion()

        val response = try {
            commandService2.execute(request = node)
                .also { response ->
                    if (log.isDebugEnabled)
                        log.debug("RESPONSE (id: '${id}'): '${toJson(response)}'.")
                }
        } catch (expected: Exception) {
            log.debug("Error.", expected)
            errorResponse(
                exception = expected,
                id = id,
                version = version
            )
        }

        return ResponseEntity(response, HttpStatus.OK)
    }

    private fun createErrorResponseEntity(expected: Exception, id: UUID = NaN): ResponseEntity<ApiResponse> {
        val response = errorResponse(
            exception = expected,
            version = GlobalProperties.App.apiVersion,
            id = id
        )
        return ResponseEntity(response, HttpStatus.OK)
    }
}
