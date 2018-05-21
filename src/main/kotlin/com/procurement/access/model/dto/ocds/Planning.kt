package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import javax.validation.Valid

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("budget", "rationale")
data class Planning(

        @Valid
        @JsonProperty("budget")
        val budget: Budget,

        @JsonProperty("rationale")
        val rationale: String?
)