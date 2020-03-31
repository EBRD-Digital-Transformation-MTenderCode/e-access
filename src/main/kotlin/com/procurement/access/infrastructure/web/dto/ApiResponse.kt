package com.procurement.access.infrastructure.web.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.enums.ResponseStatus
import java.time.LocalDateTime
import java.util.*

@JsonPropertyOrder("version", "id", "status", "result")
sealed class ApiResponse(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: UUID,

    @field:JsonProperty("version") @param:JsonProperty("version") val version: ApiVersion,

    @field:JsonProperty("result") @param:JsonProperty("result") val result: Any?
) {
    abstract val status: ResponseStatus
}

class ApiSuccessResponse(
    version: ApiVersion,
    id: UUID,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) result: Any? = null
) : ApiResponse(
    version = version,
    result = result,
    id = id
) {
    @field:JsonProperty("status")
    override val status: ResponseStatus = ResponseStatus.SUCCESS
}

class ApiErrorResponse(
    version: ApiVersion,
    id: UUID,
    result: List<Error>
) : ApiResponse(
    version = version,
    result = result,
    id = id
) {
    @field:JsonProperty("status")
    override val status: ResponseStatus = ResponseStatus.ERROR

    class Error(
        val code: String,
        val description: String,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        val details: List<Detail>? = null
    ) {
        class Detail(
            @JsonInclude(JsonInclude.Include.NON_NULL)
            val name: String? = null,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            val id: String? = null
        )
    }
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

    class Incident(
        val id: UUID,
        val date: LocalDateTime,
        val level: Fail.Incident.Level,
        val service: Service,
        val details: List<Detail>
    ) {
        class Service(val id: String, val name: String, val version: String)
        class Detail(val code: String, val description: String, val metadata: Any?)
    }
}
