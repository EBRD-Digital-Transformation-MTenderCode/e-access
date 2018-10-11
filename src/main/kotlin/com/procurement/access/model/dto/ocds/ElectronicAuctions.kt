package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude


@JsonInclude(JsonInclude.Include.NON_NULL)
data class ElectronicAuctions @JsonCreator constructor(

        val details: Set<ElectronicAuctionsDetails>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ElectronicAuctionsDetails @JsonCreator constructor(

        val id: String,

        var relatedLot: String,

        val electronicAuctionModalities: Set<ElectronicAuctionModalities>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ElectronicAuctionModalities @JsonCreator constructor(

        val eligibleMinimumDifference: Value
)

