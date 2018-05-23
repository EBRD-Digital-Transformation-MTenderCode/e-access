package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Classification(

        @JsonProperty("scheme") @NotNull
        val scheme: Scheme,

        @JsonProperty("id") @NotNull
        val id: String,

        @JsonProperty("description") @NotNull
        val description: String,

        @JsonProperty("uri")
        val uri: String?
)
