package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder("useOrdering", "usePayment", "acceptInvoicing")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
data class PnElectronicWorkflows(

        @param:JsonProperty("useOrdering")
        @get:JsonProperty("useOrdering")
        val useOrdering: Boolean?,

        @param:JsonProperty("usePayment")
        @get:JsonProperty("usePayment")
        val usePayment: Boolean?,

        @param:JsonProperty("acceptInvoicing")
        @get:JsonProperty("acceptInvoicing")
        val acceptInvoicing: Boolean?
)