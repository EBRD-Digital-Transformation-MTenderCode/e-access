package com.procurement.access.controller

import com.procurement.access.application.service.Logger
import com.procurement.access.config.GlobalProperties
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.BadRequestErrors
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.infrastructure.web.dto.ApiVersion
import com.procurement.access.model.dto.bpe.NaN
import com.procurement.access.model.dto.bpe.errorResponse
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getVersion
import com.procurement.access.service.CommandService2
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toNode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/command2")
class Command2Controller(
    private val commandService2: CommandService2,
    private val logger: Logger
) {

    @PostMapping
    fun command(
        @RequestBody requestBody: String
    ): ResponseEntity<ApiResponse> {

        logger.info("RECEIVED COMMAND: '${requestBody}'.")

        val node = requestBody.toNode()
            .onFailure {
                return responseEntity(
                    expected = BadRequestErrors.Parsing(
                        message = "Invalid request data",
                        request = requestBody,
                        exception = it.reason.exception
                    )
                )
            }

        val version = node.getVersion()
            .onFailure { versionError ->
                val id = node.getId()
                    .onFailure { return responseEntity(expected = versionError.reason) }
                return responseEntity(expected = versionError.reason, id = id)
            }

        val id = node.getId()
            .onFailure { return responseEntity(expected = it.reason, version = version) }

        val response = commandService2.execute(request = node)
            .also { response ->
                logger.info("RESPONSE (id: '${id}'): '${toJson(response)}'.")
            }

        return ResponseEntity(response, HttpStatus.OK)
    }

    private fun responseEntity(
        expected: Fail,
        id: UUID = NaN,
        version: ApiVersion = GlobalProperties.App.apiVersion
    ): ResponseEntity<ApiResponse> {
        expected.logging(logger)
        val response = errorResponse(fail = expected, id = id, version = version)
        return ResponseEntity(response, HttpStatus.OK)
    }
}
