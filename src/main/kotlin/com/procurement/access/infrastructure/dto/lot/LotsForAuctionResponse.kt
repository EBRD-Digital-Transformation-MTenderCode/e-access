package com.procurement.access.infrastructure.dto.lot

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.lot.TemporalLotId
import com.procurement.access.domain.model.money.Money
import com.procurement.access.infrastructure.bind.money.MoneyDeserializer
import com.procurement.access.infrastructure.bind.money.MoneySerializer

@JsonIgnoreProperties(ignoreUnknown = true)
data class LotsForAuctionResponse(
    @field:JsonProperty("auctionLots") @param:JsonProperty("auctionLots") val lots: List<Lot>
) {

    data class Lot(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: TemporalLotId,

        @JsonDeserialize(using = MoneyDeserializer::class)
        @JsonSerialize(using = MoneySerializer::class)
        @field:JsonProperty("value") @param:JsonProperty("value") val value: Money
    )
}
