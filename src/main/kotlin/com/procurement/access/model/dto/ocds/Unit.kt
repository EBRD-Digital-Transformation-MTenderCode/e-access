package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder("id", "name")
data class Unit(

        @JsonProperty("id")
        val id: String,

        @JsonProperty("name")
        val name: String
)