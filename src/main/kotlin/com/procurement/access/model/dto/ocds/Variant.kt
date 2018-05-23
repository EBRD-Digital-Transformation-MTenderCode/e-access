package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class Variant(

        @NotNull
        @JsonProperty("hasVariants")
        @get:JsonProperty("hasVariants")
        val hasVariants: Boolean
)