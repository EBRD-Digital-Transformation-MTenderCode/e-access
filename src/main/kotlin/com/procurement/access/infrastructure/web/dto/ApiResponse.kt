package com.procurement.access.infrastructure.web.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.enums.ResponseStatus
import com.procurement.access.infrastructure.bind.apiversion.ApiVersionDeserializer
import com.procurement.access.infrastructure.bind.apiversion.ApiVersionSerializer
import java.time.LocalDateTime
import java.util.*

sealed class ApiResponse(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: UUID,

    @JsonDeserialize(using = ApiVersionDeserializer::class)
    @JsonSerialize(using = ApiVersionSerializer::class)
    @field:JsonProperty("version") @param:JsonProperty("version") val version: ApiVersion,

    @field:JsonProperty("result") @param:JsonProperty("result") val result: Any?
) {
    abstract val status: ResponseStatus
}

class ApiSuccessResponse(
    version: ApiVersion,
    id: UUID,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) result: Any?
) : ApiResponse(
    version = version,
    result = result,
    id = id
) {
    @field:JsonProperty("status")
    override val status: ResponseStatus = ResponseStatus.SUCCESS
}

class ApiFailResponse(
    version: ApiVersion,
    id: UUID,
    result: List<Error>
) : ApiResponse(
    version = version,
    result = result,
    id = id
) {
    @field:JsonProperty("status")
    override val status: ResponseStatus = ResponseStatus.FAIL

    class Error(val code: String?, val description: String?)
}

class ApiIncidentResponse(
    version: ApiVersion,
    id: UUID,
    result: Incident
) : ApiResponse(
    version = version,
    result = result,
    id = id
) {
    @field:JsonProperty("status")
    override val status: ResponseStatus = ResponseStatus.INCIDENT

    class Incident(val id: UUID, val date: LocalDateTime, val service: Service, val errors: List<Error>) {
        class Service(val id: String, val name: String, val version: ApiVersion)
        class Error(val code: String, val description: String, val metadata: Any?)
    }
}
