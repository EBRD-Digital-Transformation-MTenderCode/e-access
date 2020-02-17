package com.procurement.access.controller

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.config.GlobalProperties
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.infrastructure.web.dto.ApiVersion
import com.procurement.access.model.dto.bpe.NaN
import com.procurement.access.model.dto.bpe.errorResponse
import com.procurement.access.service.CommandService2
import com.procurement.access.utils.getBy
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toNode
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

        val node: JsonNode = try {
            requestBody.toNode()
        } catch (expected: Exception) {
            log.debug("Error.", expected)
            return createErrorResponseEntity(
                expected = expected
            )
        }

        val id = try {
            UUID.fromString(node.getBy(parameter = "id").asText())
        } catch (expected: Exception) {
            log.debug("Error.", expected)
            return createErrorResponseEntity(
                expected = expected
            )
        }

        val version = try {
            ApiVersion.valueOf(node.getBy(parameter = "version").asText())
        } catch (expected: Exception) {
            log.debug("Error.", expected)
            return createErrorResponseEntity(
                id = id,
                expected = expected
            )
        }

        try {
            node.getBy(parameter = "params")
        } catch (expected: Exception) {
            log.debug("Error.", expected)
            return createErrorResponseEntity(
                id = id,
                expected = expected,
                version = version
            )
        }

        val response = try {
            commandService2.execute(request = node)
                .also { response ->
                    if (log.isDebugEnabled)
                        log.debug("RESPONSE (id: '${id}'): '${toJson(response)}'.")
                }
        } catch (expected: Exception) {
            log.debug("Error.", expected)
            return createErrorResponseEntity(
                id = id,
                expected = expected,
                version = version
            )
        }

        return ResponseEntity(response, HttpStatus.OK)
    }

    private fun createErrorResponseEntity(
        expected: Exception,
        id: UUID = NaN,
        version : ApiVersion = GlobalProperties.App.apiVersion
    ): ResponseEntity<ApiResponse> {
        val response = errorResponse(
            exception = expected,
            version = version,
            id = id
        )
        return ResponseEntity(response, HttpStatus.OK)
    }
}
