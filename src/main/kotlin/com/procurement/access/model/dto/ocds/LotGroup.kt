package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class LotGroup(

        @NotNull
        @JsonProperty("optionToCombine")
        @get:JsonProperty("optionToCombine")
        val optionToCombine: Boolean
)