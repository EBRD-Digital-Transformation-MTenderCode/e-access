package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Item @JsonCreator constructor(

        var id: String?,

        val description: String?,

        @field:Valid
        val classification: Classification,

        @field:Valid
        val additionalClassifications: HashSet<Classification>?,

        val quantity: BigDecimal,

        @field:Valid
        val unit: Unit,

        var relatedLot: String
)