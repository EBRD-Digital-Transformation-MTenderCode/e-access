package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Planning(

        @JsonProperty("budget") @Valid @NotNull
        val budget: Budget,

        @JsonProperty("rationale")
        val rationale: String?
)