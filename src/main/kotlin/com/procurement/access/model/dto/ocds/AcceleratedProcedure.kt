package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty

data class AcceleratedProcedure(

        @JsonProperty("isAcceleratedProcedure")
        @get:JsonProperty("isAcceleratedProcedure")
        val isAcceleratedProcedure: Boolean
)