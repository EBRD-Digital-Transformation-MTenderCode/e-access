package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class AcceleratedProcedure(

        @JsonProperty("isAcceleratedProcedure") @NotNull
        @get:JsonProperty("isAcceleratedProcedure")
        val isAcceleratedProcedure: Boolean
)