package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ContactPoint(

        @JsonProperty("name") @NotNull
        val name: String,

        @JsonProperty("email") @NotNull
        val email: String,

        @JsonProperty("telephone") @NotNull
        val telephone: String,

        @JsonProperty("faxNumber")
        val faxNumber: String?,

        @JsonProperty("url") @NotNull
        val url: String
)
