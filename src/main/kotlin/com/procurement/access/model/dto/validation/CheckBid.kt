package com.procurement.access.model.dto.validation

import com.fasterxml.jackson.annotation.JsonCreator
import com.procurement.access.model.dto.ocds.Value

data class CheckBid @JsonCreator constructor(

        val bid: BidCheck
)

data class BidCheck @JsonCreator constructor(

        val value: Value?,

        var relatedLots: List<String>
)