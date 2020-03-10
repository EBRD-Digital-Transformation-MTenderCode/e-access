package com.procurement.access.model.dto.bpe

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import com.procurement.access.config.GlobalProperties
import com.procurement.access.domain.EnumElementProvider
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.Fail.Error
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Action
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.domain.util.bind
import com.procurement.access.infrastructure.web.dto.ApiErrorResponse
import com.procurement.access.infrastructure.web.dto.ApiIncidentResponse
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.infrastructure.web.dto.ApiVersion
import com.procurement.access.utils.tryToObject
import java.time.LocalDateTime
import java.util.*

enum class Command2Type(@JsonValue override val key: String) : EnumElementProvider.Key, Action {
    GET_LOT_IDS("getLotIds");

    override fun toString(): String = key

    companion object : EnumElementProvider<Command2Type>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = Command2Type.orThrow(name)
    }
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
            incident = fail
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
    incident: Fail.Incident
): ApiIncidentResponse {
    return ApiIncidentResponse(
        id = id,
        version = version,
        result = generateIncident(fail = incident)
    )
}

fun generateIncident(fail: Fail.Incident): ApiIncidentResponse.Incident {
    return ApiIncidentResponse.Incident(
        id = UUID.randomUUID(),
        date = LocalDateTime.now(),
        level = fail.level,
        details = listOf(
            ApiIncidentResponse.Incident.Detail(
                code = "${fail.code}/${GlobalProperties.service.id}",
                description = fail.description,
                metadata = null
            )
        ),
        service = ApiIncidentResponse.Incident.Service(
            id = GlobalProperties.service.id,
            version = GlobalProperties.service.version,
            name = GlobalProperties.service.name
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
                    DataErrors.Validation.DataFormatMismatch(
                        name = "version",
                        actualValue = value,
                        expectedFormat = "00.00.00"
                    )
                }
            }
        }
}

fun JsonNode.getAction(): Result<Command2Type, DataErrors> {
    return this.getAttribute("action")
        .bind {
            val value = it.asText()
            Command2Type.orNull(value)?.asSuccess<Command2Type, DataErrors>() ?: Result.failure(
                DataErrors.Validation.UnknownValue(
                    name = "action",
                    actualValue = value,
                    expectedValues = Command2Type.allowedValues
                )
            )
        }
}

private fun asUUID(value: String): Result<UUID, DataErrors> =
    try {
        Result.success<UUID>(UUID.fromString(value))
    } catch (exception: IllegalArgumentException) {
        Result.failure(
            DataErrors.Validation.DataFormatMismatch(
                name = "id",
                expectedFormat = "uuid",
                actualValue = value
            )
        )
    }

fun JsonNode.getAttribute(name: String): Result<JsonNode, DataErrors> {
    return if (has(name)) {
        val attr = get(name)
        if (attr !is NullNode)
            Result.success(attr)
        else
            Result.failure(
                DataErrors.Validation.DataTypeMismatch(name = "$attr", actualType = "null", expectedType = "not null")
            )
    } else
        Result.failure(
            DataErrors.Validation.MissingRequiredAttribute(name = name)
        )
}

fun <T : Any> JsonNode.tryGetParams(target: Class<T>): Result<T, DataErrors> =
    getAttribute("params").bind { node ->
        when (val result = node.tryToObject(target)) {
            is Result.Success -> result
            is Result.Failure -> result.mapError {
                DataErrors.Parsing(result.error)
            }
        }
    }

fun JsonNode.hasParams(): ValidationResult<DataErrors> =
    if (this.has("params"))
        ValidationResult.ok()
    else
        ValidationResult.error(
            DataErrors.Validation.MissingRequiredAttribute(name = "params")
        )
