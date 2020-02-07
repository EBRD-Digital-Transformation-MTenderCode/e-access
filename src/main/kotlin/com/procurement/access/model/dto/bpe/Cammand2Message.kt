package com.procurement.access.model.dto.bpe

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.config.GlobalProperties
import com.procurement.access.exception.EnumException
import com.procurement.access.exception.ErrorException
import com.procurement.access.infrastructure.web.dto.ApiErrorResponse
import com.procurement.access.infrastructure.web.dto.ApiVersion

data class Command2Message @JsonCreator constructor(

    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
    @field:JsonProperty("action") @param:JsonProperty("action") val action: Command2Type,
    @field:JsonProperty("params") @param:JsonProperty("params") val params: JsonNode,
    @field:JsonProperty("version") @param:JsonProperty("version") val version: ApiVersion
)

enum class Command2Type(private val value: String) {
    GET_LOT_IDS_BY_STATES("getLotIdsByStates");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }
}

fun errorResponse(exception: Exception, id: String, version: ApiVersion): ApiErrorResponse =
    when (exception) {
        is ErrorException -> getApiErrorResponse(
            id = id,
            version = version,
            code = exception.code,
            message = exception.message!!
        )
        is EnumException  -> getApiErrorResponse(
            id = id,
            version = version,
            code = exception.code,
            message = exception.message!!
        )
        else              -> getApiErrorResponse(
            id = id,
            version = version,
            code = "00.00",
            message = exception.message!!
        )
    }

private fun getApiErrorResponse(id: String, version: ApiVersion, code: String, message: String): ApiErrorResponse {
    return ApiErrorResponse(
        errors = listOf(
            ApiErrorResponse.Error(
                code = "400.${GlobalProperties.serviceId}." + code,
                description = message
            )
        ),
        id = id,
        version = version
    )
}
