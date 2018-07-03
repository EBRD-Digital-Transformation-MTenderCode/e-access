package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PnElectronicWorkflows @JsonCreator constructor(

        @get:JsonProperty("useOrdering")
        val useOrdering: Boolean?,

        @get:JsonProperty("usePayment")
        val usePayment: Boolean?,

        @get:JsonProperty("acceptInvoicing")
        val acceptInvoicing: Boolean?
)