package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(value = JsonInclude.Include.NON_NULL)
data class PnElectronicWorkflows(

        @JsonProperty("useOrdering")
        @get:JsonProperty("useOrdering")
        val useOrdering: Boolean?,

        @JsonProperty("usePayment")
        @get:JsonProperty("usePayment")
        val usePayment: Boolean?,

        @JsonProperty("acceptInvoicing")
        @get:JsonProperty("acceptInvoicing")
        val acceptInvoicing: Boolean?
)