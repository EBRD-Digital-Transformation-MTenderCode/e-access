package com.procurement.access.model.dto.response


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.money.Money
import com.procurement.access.infrastructure.bind.money.MoneyDeserializer
import com.procurement.access.infrastructure.bind.money.MoneySerializer

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetLotsAuctionResponse(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
        @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>
    ) {
        data class Lot(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: LotId,
            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,

            @JsonDeserialize(using = MoneyDeserializer::class)
            @JsonSerialize(using = MoneySerializer::class)
            @field:JsonProperty("value") @param:JsonProperty("value") val value: Money
        )
    }
}