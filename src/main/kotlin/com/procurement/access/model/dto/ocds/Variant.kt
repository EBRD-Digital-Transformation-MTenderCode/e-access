package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty

data class Variant(

        @param:JsonProperty("hasVariants")
        @get:JsonProperty("hasVariants")
        val hasVariants: Boolean
)