package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Identifier(

        @JsonProperty("id") @NotNull
        val id: String,

        @JsonProperty("scheme") @NotNull
        val scheme: String,

        @JsonProperty("legalName") @NotNull
        val legalName: String,

        @JsonProperty("uri") @NotNull
        val uri: String
)