package com.procurement.access.infrastructure.handler.find.auction


import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.infrastructure.bind.amount.AmountDeserializer
import com.procurement.access.infrastructure.bind.amount.AmountSerializer
import java.math.BigDecimal

data class FindAuctionsResult(
    @param:JsonProperty("tender") @field:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @param:JsonProperty("electronicAuctions") @field:JsonProperty("electronicAuctions") val electronicAuctions: ElectronicAuctions
    ) {
        data class ElectronicAuctions(
            @param:JsonProperty("details") @field:JsonProperty("details") val details: List<Detail>
        ) {
            data class Detail(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                @param:JsonProperty("relatedLot") @field:JsonProperty("relatedLot") val relatedLot: String,
                @param:JsonProperty("electronicAuctionModalities") @field:JsonProperty("electronicAuctionModalities") val electronicAuctionModalities: List<ElectronicAuctionModality>
            ) {
                data class ElectronicAuctionModality(
                    @param:JsonProperty("eligibleMinimumDifference") @field:JsonProperty("eligibleMinimumDifference") val eligibleMinimumDifference: EligibleMinimumDifference
                ) {
                    data class EligibleMinimumDifference(
                        @JsonDeserialize(using = AmountDeserializer::class)
                        @JsonSerialize(using = AmountSerializer::class)
                        @param:JsonProperty("amount") @field:JsonProperty("amount") val amount: BigDecimal,

                        @param:JsonProperty("currency") @field:JsonProperty("currency") val currency: String
                    )
                }
            }
        }
    }
}