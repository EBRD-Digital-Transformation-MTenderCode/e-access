package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class ElectronicWorkflows(

        @JsonProperty("useOrdering")  @NotNull
        @get:JsonProperty("useOrdering")
        val useOrdering: Boolean,

        @JsonProperty("usePayment")  @NotNull
        @get:JsonProperty("usePayment")
        val usePayment: Boolean,

        @JsonProperty("acceptInvoicing")  @NotNull
        @get:JsonProperty("acceptInvoicing")
        val acceptInvoicing: Boolean
)