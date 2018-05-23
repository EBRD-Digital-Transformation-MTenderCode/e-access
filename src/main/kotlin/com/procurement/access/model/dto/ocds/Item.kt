package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Item(

        @JsonProperty("id")
        var id: String?,

        @JsonProperty("description")
        val description: String?,

        @JsonProperty("classification") @Valid @NotNull
        val classification: Classification,

        @JsonProperty("additionalClassifications") @Valid
        val additionalClassifications: HashSet<Classification>?,

        @JsonProperty("quantity") @NotNull
        val quantity: BigDecimal,

        @JsonProperty("unit") @Valid @NotNull
        val unit: Unit,

        @JsonProperty("relatedLot") @NotNull
        var relatedLot: String
)