package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class RecurrentProcurement @JsonCreator constructor(

        @field:NotNull
        @get:JsonProperty("isRecurrent")
        val isRecurrent: Boolean
)