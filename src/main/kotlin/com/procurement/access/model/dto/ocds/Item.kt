package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.util.*
import javax.validation.Valid

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Item @JsonCreator constructor(

        var id: String?,

        var description: String?,

        @field:Valid
        val classification: Classification,

        @field:Valid
        val additionalClassifications: HashSet<Classification>?,

        val quantity: BigDecimal,

        @field:Valid
        val unit: Unit,

        var relatedLot: String
)