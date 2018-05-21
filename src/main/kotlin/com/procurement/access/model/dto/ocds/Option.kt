package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty

data class Option(

        @JsonProperty("hasOptions")
        @get:JsonProperty("hasOptions")
        val hasOptions: Boolean?
)