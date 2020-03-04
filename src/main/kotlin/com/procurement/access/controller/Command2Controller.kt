package com.procurement.access.controller

import com.procurement.access.config.GlobalProperties
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.infrastructure.web.dto.ApiVersion
import com.procurement.access.model.dto.bpe.NaN
import com.procurement.access.model.dto.bpe.errorResponse
import com.procurement.access.model.dto.bpe.getAction
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getVersion
import com.procurement.access.model.dto.bpe.hasParams
import com.procurement.access.service.CommandService2
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
            log.debug("RECEIVED COMMAND: '${requestBody}'.")

        val node = requestBody.toNode()
            .doOnError { error -> return createErrorResponseEntity(expected = error) }
            .get

        val id = node.getId()
            .doOnError { error -> return createErrorResponseEntity(expected = error) }
            .get

        val version = node.getVersion()
            .doOnError { error -> return createErrorResponseEntity(id = id, expected = error) }
            .get

        node.getAction()
            .doOnError { error -> return createErrorResponseEntity(id = id, expected = error, version = version) }

        val hasParams = node.hasParams()
        if (hasParams.isError)
            return createErrorResponseEntity(id = id, expected = hasParams.error, version = version)

        val response = commandService2.execute(request = node)
            .also { response ->
                if (log.isDebugEnabled)
                    log.debug("RESPONSE (id: '${id}'): '${toJson(response)}'.")
            }

        return ResponseEntity(response, HttpStatus.OK)
    }

    private fun createErrorResponseEntity(
        expected: Fail,
        id: UUID = NaN,
        version: ApiVersion = GlobalProperties.App.apiVersion
    ): ResponseEntity<ApiResponse> {
        val response = errorResponse(
            fail = expected,
            version = version,
            id = id
        )
        return ResponseEntity(response, HttpStatus.OK)
    }
}
