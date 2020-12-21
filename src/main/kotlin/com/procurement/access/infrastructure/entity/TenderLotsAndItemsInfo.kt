package com.procurement.access.infrastructure.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.infrastructure.bind.amount.positive.AmountPositiveDeserializer
import com.procurement.access.infrastructure.bind.amount.positive.AmountPositiveSerializer
import java.math.BigDecimal
import java.time.LocalDateTime

data class TenderLotsAndItemsInfo(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("items") @param:JsonProperty("items") val items: List<Item>?,
    ) {
        data class Lot(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: LotId,
            @field:JsonProperty("value") @param:JsonProperty("value") val value: Value,
            @field:JsonProperty("contractPeriod") @param:JsonProperty("contractPeriod") val contractPeriod: ContractPeriod,
        ){
            data class Value(
                @JsonDeserialize(using = AmountPositiveDeserializer::class)
                @JsonSerialize(using = AmountPositiveSerializer::class)
                @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: BigDecimal,

                @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: String
            )

            data class ContractPeriod(
                @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,
                @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
            )
        }

        data class Item(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: LotId
        )
    }
}