package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.Classification
import com.procurement.access.model.dto.ocds.Unit
import java.math.BigDecimal
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PnItem(

        @JsonProperty("id") @NotNull
        var id: String,

        @JsonProperty("description")
        val description: String?,

        @JsonProperty("classification") @Valid @NotNull
        val classification: Classification,

        @JsonProperty("additionalClassifications") @Valid
        val additionalClassifications: HashSet<Classification>?,

        @JsonProperty("quantity")
        val quantity: BigDecimal?,

        @JsonProperty("unit") @Valid
        val unit: Unit?,

        @JsonProperty("relatedLot") @NotNull
        var relatedLot: String
)