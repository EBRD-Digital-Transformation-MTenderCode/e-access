package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType


@JsonInclude(JsonInclude.Include.NON_NULL)
data class ElectronicAuctions @JsonCreator constructor(

        val details: List<ElectronicAuctionsDetails>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ElectronicAuctionsDetails @JsonCreator constructor(

        val id: String,

        var relatedLot: String,

        val electronicAuctionModalities: List<ElectronicAuctionModalities>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ElectronicAuctionModalities @JsonCreator constructor(

        val eligibleMinimumDifference: Value
)

fun ElectronicAuctions.validate(): ElectronicAuctions {
    if (this.details.isEmpty()) throw ErrorException(ErrorType.INVALID_AUCTION_IS_EMPTY)
    return this
}