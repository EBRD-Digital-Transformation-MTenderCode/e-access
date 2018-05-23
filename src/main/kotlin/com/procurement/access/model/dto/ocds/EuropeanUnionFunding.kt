package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EuropeanUnionFunding(

        @JsonProperty("projectIdentifier") @NotNull
        val projectIdentifier: String,

        @JsonProperty("projectName") @NotNull
        val projectName: String,

        @JsonProperty("uri")
        val uri: String?
)