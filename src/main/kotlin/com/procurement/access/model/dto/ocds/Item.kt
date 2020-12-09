package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Item @JsonCreator constructor(

    var id: String?,

    val internalId: String?,

    var description: String?,

    val classification: Classification,

    val additionalClassifications: List<Classification>?,

    val quantity: BigDecimal,

    val unit: Unit,

    var relatedLot: String
)