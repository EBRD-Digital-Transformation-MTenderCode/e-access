package com.procurement.access.model.dto.bpe

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import com.procurement.access.config.GlobalProperties
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.Fail.Error
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Action
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.domain.util.bind
import com.procurement.access.infrastructure.web.dto.ApiErrorResponse
import com.procurement.access.infrastructure.web.dto.ApiIncidentResponse
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.infrastructure.web.dto.ApiVersion
import com.procurement.access.utils.tryToObject
import java.time.LocalDateTime
import java.util.*

enum class Command2Type(@JsonValue override val value: String) : Action {
    GET_LOT_IDS("getLotIds");

    companion object {
        private val elements: Map<String, Command2Type> = values().associateBy { it.value.toUpperCase() }

        fun tryOf(value: String): Result<Command2Type, String> = elements[value.toUpperCase()]
            ?.let {
                Result.success(it)
            }
            ?: Result.failure(
                "Unknown value for enumType ${Command2Type::class.java.canonicalName}: " +
                    "$value, Allowed values are ${values().joinToString { it.value }}"
            )
    }

    override fun toString() = value
}

fun errorResponse(fail: Fail, id: UUID = NaN, version: ApiVersion): ApiResponse =
    when (fail) {
        is Error -> getApiFailResponse(
            id = id,
            version = version,
            code = fail.code,
            message = fail.description
        )
        is Fail.Incident -> getApiIncidentResponse(
            id = id,
            version = version,
            code = fail.code,
            message = fail.description
        )
    }

private fun getApiFailResponse(
    id: UUID,
    version: ApiVersion,
    code: String,
    message: String
): ApiErrorResponse {
    return ApiErrorResponse(
        id = id,
        version = version,
        result = listOf(
            ApiErrorResponse.Error(
                code = "${code}/${GlobalProperties.service.id}",
                description = message
            )
        )
    )
}

private fun getApiIncidentResponse(
    id: UUID,
    version: ApiVersion,
    code: String,
    message: String
): ApiIncidentResponse {
    return ApiIncidentResponse(
        id = id,
        version = version,
        result = ApiIncidentResponse.Incident(
            id = UUID.randomUUID(),
            date = LocalDateTime.now(),
            errors = listOf(
                ApiIncidentResponse.Incident.Error(
                    code = "${code}/${GlobalProperties.service.id}",
                    description = message,
                    metadata = null
                )
            ),
            service = ApiIncidentResponse.Incident.Service(
                id = GlobalProperties.service.id,
                version = GlobalProperties.service.version,
                name = GlobalProperties.service.name
            )
        )
    )
}

val NaN: UUID
    get() = UUID(0, 0)

fun JsonNode.getId(): Result<UUID, DataErrors> {
    return this.getAttribute("id")
        .bind {
            val value = it.asText()
            asUUID(value)
        }
}

fun JsonNode.getVersion(): Result<ApiVersion, DataErrors> {
    return this.getAttribute("version")
        .bind {
            val value = it.asText()
            when (val result = ApiVersion.tryOf(value)) {
                is Result.Success -> result
                is Result.Failure -> result.mapError {
                    DataErrors.DataFormatMismatch(attributeName = result.error)
                }
            }
        }
}

fun JsonNode.getAction(): Result<Command2Type, DataErrors> {
    return this.getAttribute("action")
        .bind {
            val value = it.asText()
            when (val result = Command2Type.tryOf(value)) {
                is Result.Success -> result
                is Result.Failure -> result.mapError {
                    DataErrors.DataFormatMismatch(attributeName = result.error)
                }
            }
        }
}

private fun asUUID(value: String): Result<UUID, DataErrors> =
    try {
        Result.success<UUID>(UUID.fromString(value))
    } catch (exception: IllegalArgumentException) {
        Result.failure(
            DataErrors.DataFormatMismatch(attributeName = "id")
        )
    }

fun JsonNode.getAttribute(name: String): Result<JsonNode, DataErrors> {
    return if (has(name)) {
        val attr = get(name)
        if (attr !is NullNode)
            Result.success(attr)
        else
            Result.failure(
                DataErrors.DataTypeMismatch(attributeName = "$attr")
            )
    } else
        Result.failure(
            DataErrors.MissingRequiredAttribute(attributeName = name)
        )
}

fun <T : Any> JsonNode.tryGetParams(target: Class<T>): Result<T, DataErrors> =
    getAttribute("params").bind {node->
        when (val result = node.tryToObject(target)) {
            is Result.Success -> result
            is Result.Failure -> result.mapError {
                DataErrors.DataFormatMismatch(attributeName = result.error)
            }
        }
    }

fun JsonNode.hasParams(): ValidationResult<DataErrors> =
    if (this.has("params"))
        ValidationResult.ok()
    else
        ValidationResult.error(
            DataErrors.MissingRequiredAttribute(attributeName = "params")
        )
