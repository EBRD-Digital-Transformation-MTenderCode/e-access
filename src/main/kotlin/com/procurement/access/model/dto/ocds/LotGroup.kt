package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty

data class LotGroup(

        @param:JsonProperty("optionToCombine")
        @get:JsonProperty("optionToCombine")
        val optionToCombine: Boolean
)