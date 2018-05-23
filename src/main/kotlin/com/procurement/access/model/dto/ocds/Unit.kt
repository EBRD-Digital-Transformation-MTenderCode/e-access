package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Unit(

        @JsonProperty("id") @NotNull
        val id: String,

        @JsonProperty("name") @NotNull
        val name: String
)