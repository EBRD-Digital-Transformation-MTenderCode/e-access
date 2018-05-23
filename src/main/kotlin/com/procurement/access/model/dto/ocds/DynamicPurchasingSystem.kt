package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class DynamicPurchasingSystem(

        @NotNull
        @JsonProperty("hasDynamicPurchasingSystem")
        @get:JsonProperty("hasDynamicPurchasingSystem")
        val hasDynamicPurchasingSystem: Boolean
)