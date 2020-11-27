package com.procurement.access.infrastructure.api.v1

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId

sealed class ApiResponseV1 {
    abstract val version: ApiVersion
    abstract val id: CommandId

    class Success(
        @field:JsonProperty("version") @param:JsonProperty("version") override val version: ApiVersion,
        @field:JsonProperty("id") @param:JsonProperty("id") override val id: CommandId,
        @field:JsonProperty("data") @param:JsonProperty("data") val data: Any?
    ) : ApiResponseV1()

    class Failure(
        @field:JsonProperty("version") @param:JsonProperty("version") override val version: ApiVersion,
        @field:JsonProperty("id") @param:JsonProperty("id") override val id: CommandId,
        @field:JsonProperty("errors") @param:JsonProperty("errors") val errors: List<Error>
    ) : ApiResponseV1() {

        data class Error(
            @field:JsonProperty("code") @param:JsonProperty("code") val code: String,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String
        )
    }
}
