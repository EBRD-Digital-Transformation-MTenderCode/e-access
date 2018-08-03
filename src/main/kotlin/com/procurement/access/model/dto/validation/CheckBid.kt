package com.procurement.access.model.dto.validation

import com.fasterxml.jackson.annotation.JsonCreator
import com.procurement.access.model.dto.ocds.Value
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

data class CheckBid @JsonCreator constructor(

    @field:Valid
    val bid: BidCheck
)

data class BidCheck @JsonCreator constructor(

        @field:Valid
        val value: Value?,

        @field:NotEmpty
        var relatedLots: List<String>
)