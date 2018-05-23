package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.Classification
import com.procurement.access.model.dto.ocds.Unit
import java.math.BigDecimal
import java.util.*
import javax.validation.Valid

@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder("id", "description", "classification", "additionalClassifications", "quantity", "unit", "relatedLot")
data class PnItem(

        @JsonProperty("id")
        var id: String,

        @JsonProperty("description")
        val description: String?,

        @JsonProperty("classification") @Valid
        val classification: Classification,

        @JsonProperty("additionalClassifications") @Valid
        val additionalClassifications: HashSet<Classification>?,

        @JsonProperty("quantity")
        val quantity: BigDecimal?,

        @JsonProperty("unit") @Valid
        val unit: Unit?,

        @JsonProperty("relatedLot")
        var relatedLot: String
)