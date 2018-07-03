package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class ElectronicWorkflows @JsonCreator constructor(

        @field:NotNull
        @get:JsonProperty("useOrdering")
        val useOrdering: Boolean,

        @field:NotNull
        @get:JsonProperty("usePayment")
        val usePayment: Boolean,

        @field:NotNull
        @get:JsonProperty("acceptInvoicing")
        val acceptInvoicing: Boolean
)