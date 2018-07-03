package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Option @JsonCreator constructor(

        @get:JsonProperty("hasOptions")
        val hasOptions: Boolean?
)