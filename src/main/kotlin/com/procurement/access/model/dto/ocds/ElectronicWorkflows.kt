package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty

//@JsonPropertyOrder("useOrdering", "usePayment", "acceptInvoicing")
data class ElectronicWorkflows(

        @JsonProperty("useOrdering")
        @get:JsonProperty("useOrdering")
        val useOrdering: Boolean,

        @JsonProperty("usePayment")
        @get:JsonProperty("usePayment")
        val usePayment: Boolean,

        @JsonProperty("acceptInvoicing")
        @get:JsonProperty("acceptInvoicing")
        val acceptInvoicing: Boolean
)