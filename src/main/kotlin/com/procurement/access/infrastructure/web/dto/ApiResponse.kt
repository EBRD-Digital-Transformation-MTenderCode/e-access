package com.procurement.access.infrastructure.web.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.enums.ResponseStatus
import com.procurement.access.infrastructure.bind.apiversion.ApiVersionDeserializer
import com.procurement.access.infrastructure.bind.apiversion.ApiVersionSerializer

sealed class ApiResponse {
    abstract val id: String
    abstract val version: ApiVersion
}

class ApiErrorResponse(
    @field:JsonProperty("id") @param:JsonProperty("id") override val id: String,

    @JsonDeserialize(using = ApiVersionDeserializer::class)
    @JsonSerialize(using = ApiVersionSerializer::class)
    @field:JsonProperty("version") @param:JsonProperty("version") override val version: ApiVersion,

    @field:JsonProperty("errors") @param:JsonProperty("errors") val errors: List<Error>

) : ApiResponse() {
    data class Error(

        @field:JsonProperty("code") @param:JsonProperty("code") val code: String,

        @field:JsonProperty("description") @param:JsonProperty("description") val description: String
    )
}


@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiSuccessResponse2(

    @JsonDeserialize(using = ApiVersionDeserializer::class)
    @JsonSerialize(using = ApiVersionSerializer::class)
    @field:JsonProperty("version") @param:JsonProperty("version") override val version: ApiVersion,

    @field:JsonProperty("id") @param:JsonProperty("id") override val id: String,

    @field:JsonProperty("status") @param:JsonProperty("status") val status: ResponseStatus,

    @field:JsonProperty("result") @param:JsonProperty("result") val result: Any?

) : ApiResponse()
