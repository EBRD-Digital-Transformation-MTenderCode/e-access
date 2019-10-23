package com.procurement.access.infrastructure.dto.lot

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.infrastructure.bind.amount.AmountDeserializer
import com.procurement.access.infrastructure.bind.amount.AmountSerializer
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class LotsForAuctionRequest(
    @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>
) {

    data class Lot(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: LotId,
        @field:JsonProperty("value") @param:JsonProperty("value") val value: Value
    ) {

        data class Value(
            @JsonDeserialize(using = AmountDeserializer::class)
            @JsonSerialize(using = AmountSerializer::class)
            @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: BigDecimal,

            @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: String
        )
    }
}