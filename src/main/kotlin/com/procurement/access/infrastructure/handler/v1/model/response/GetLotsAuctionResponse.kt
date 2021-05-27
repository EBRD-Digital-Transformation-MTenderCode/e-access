package com.procurement.access.infrastructure.handler.v1.model.response


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.model.dto.databinding.MoneyDeserializer
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetLotsAuctionResponse(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
        @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("value") @param:JsonProperty("value") val value: Value?
    ) {
        data class Lot(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: LotId,
            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("value") @param:JsonProperty("value") val value: Value?
        ) {
            data class Value(
                @field:JsonDeserialize(using = MoneyDeserializer::class)
                @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: BigDecimal?,

                @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: String
            )
        }

        data class Value(
            @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: String
        )

    }
}