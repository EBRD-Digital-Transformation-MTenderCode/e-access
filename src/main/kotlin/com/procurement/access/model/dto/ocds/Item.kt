package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.math.BigDecimal
import java.util.*
import javax.validation.Valid

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("id", "description", "classification", "additionalClassifications", "quantity", "unit", "relatedLot")
data class Item(

        @JsonProperty("id")
        val id: String?,

        @JsonProperty("description")
        val description: String?,

        @Valid
        @JsonProperty("classification")
        val classification: Classification,

        @Valid
        @JsonProperty("additionalClassifications")
        val additionalClassifications: HashSet<Classification>,

        @JsonProperty("quantity")
        val quantity: BigDecimal,

        @Valid
        @JsonProperty("unit")
        val unit: Unit,

        @param:JsonProperty("relatedLot")
        val relatedLot: String
)