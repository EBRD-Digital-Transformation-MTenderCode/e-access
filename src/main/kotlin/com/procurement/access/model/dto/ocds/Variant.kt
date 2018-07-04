package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class Variant @JsonCreator constructor(

        @field:NotNull
        @get:JsonProperty("hasVariants")
        val hasVariants: Boolean
)