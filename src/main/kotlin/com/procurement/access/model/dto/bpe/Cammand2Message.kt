package com.procurement.access.model.dto.bpe

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.config.GlobalProperties
import com.procurement.access.exception.EnumException
import com.procurement.access.exception.ErrorException
import com.procurement.access.infrastructure.web.dto.ApiFailResponse
import com.procurement.access.infrastructure.web.dto.ApiIncidentResponse
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.infrastructure.web.dto.ApiVersion
import java.time.LocalDateTime
import java.util.*

data class Command2Message @JsonCreator constructor(

    @field:JsonProperty("id") @param:JsonProperty("id") val id: UUID,
    @field:JsonProperty("action") @param:JsonProperty("action") val action: Command2Type,
    @field:JsonProperty("params") @param:JsonProperty("params") val params: JsonNode,
    @field:JsonProperty("version") @param:JsonProperty("version") val version: ApiVersion
)

enum class Command2Type(private val value: String) {
    GET_LOT_IDS("getLotIds");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }
}

fun errorResponse(exception: Exception, id: UUID = NaN, version: ApiVersion): ApiResponse =
    when (exception) {
        is ErrorException -> getApiFailResponse(
            id = id,
            version = version,
            code = exception.code,
            message = exception.message!!
        )
        is EnumException  -> getApiIncidentResponse(
            id = id,
            version = version,
            code = exception.code,
            message = exception.message!!
        )
        else              -> getApiIncidentResponse(
            id = id,
            version = version,
            code = "00.00",
            message = exception.message!!
        )
    }

private fun getApiFailResponse(
    id: UUID,
    version: ApiVersion,
    code: String,
    message: String
): ApiFailResponse {
    return ApiFailResponse(
        id = id,
        version = version,
        result = listOf(
            ApiFailResponse.Error(
                code = "400.${GlobalProperties.serviceId}." + code,
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
                    code = "400.${GlobalProperties.serviceId}." + code,
                    description = message,
                    metadata = null
                )
            ),
            service = ApiIncidentResponse.Incident.Service(
                id = GlobalProperties.serviceId,
                version = version,
                name = GlobalProperties.serviceName
            )
        )
    )
}

val NaN: UUID
    get() = UUID(0, 0)
