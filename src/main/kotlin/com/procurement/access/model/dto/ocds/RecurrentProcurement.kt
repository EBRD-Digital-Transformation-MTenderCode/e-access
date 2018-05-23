package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class RecurrentProcurement(

        @NotNull
        @JsonProperty("isRecurrent")
        @get:JsonProperty("isRecurrent")
        val isRecurrent: Boolean
)