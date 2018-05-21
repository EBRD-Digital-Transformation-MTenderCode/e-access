package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty

data class DynamicPurchasingSystem(

        @JsonProperty("hasDynamicPurchasingSystem")
        @get:JsonProperty("hasDynamicPurchasingSystem")
        val hasDynamicPurchasingSystem: Boolean
)