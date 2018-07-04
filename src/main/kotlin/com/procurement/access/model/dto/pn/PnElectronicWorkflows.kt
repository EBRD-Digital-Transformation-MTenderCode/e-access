package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.model.dto.databinding.BooleansDeserializer

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PnElectronicWorkflows @JsonCreator constructor(

        @field:JsonDeserialize(using = BooleansDeserializer::class)
        @get:JsonProperty("useOrdering")
        val useOrdering: Boolean?,

        @field:JsonDeserialize(using = BooleansDeserializer::class)
        @get:JsonProperty("usePayment")
        val usePayment: Boolean?,

        @field:JsonDeserialize(using = BooleansDeserializer::class)
        @get:JsonProperty("acceptInvoicing")
        val acceptInvoicing: Boolean?
)