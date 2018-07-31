package com.procurement.access.model.dto.bids

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.Value
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

data class CheckBidRQDto @JsonCreator constructor(

    @field:Valid
    val bid: CheckBidDto
)

data class CheckBidDto @JsonCreator constructor(

        @field:Valid
        val value: Value?,

        @field:NotEmpty
        var relatedLot: List<String>
)