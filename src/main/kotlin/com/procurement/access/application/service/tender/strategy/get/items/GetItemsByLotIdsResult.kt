package com.procurement.access.application.service.tender.strategy.get.items

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.infrastructure.bind.quantity.QuantityDeserializer
import com.procurement.access.infrastructure.bind.quantity.QuantitySerializer
import com.procurement.access.infrastructure.entity.CNEntity
import java.math.BigDecimal

data class GetItemsByLotIdsResult(
    @param:JsonProperty("tender") @field:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @param:JsonProperty("items") @field:JsonProperty("items") val items: List<Item>
    ) {
        data class Item(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("internalId") @param:JsonProperty("internalId") val internalId: String?,

            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,

            @JsonDeserialize(using = QuantityDeserializer::class)
            @JsonSerialize(using = QuantitySerializer::class)
            @field:JsonProperty("quantity") @param:JsonProperty("quantity") val quantity: BigDecimal,

            @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification,

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @field:JsonProperty("additionalClassifications") @param:JsonProperty("additionalClassifications") val additionalClassifications: List<Classification>?,

            @field:JsonProperty("unit") @param:JsonProperty("unit") val unit: Unit,
            @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: String
        ) {

            data class Classification(
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: Scheme,
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String
            )

            data class Unit(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("name") @param:JsonProperty("name") val name: String
            )
        }
    }

    companion object {

        fun fromDomain(item: CNEntity.Tender.Item): Tender.Item =
            Tender.Item(
                id = item.id,
                internalId = item.internalId,
                description = item.description,
                quantity = item.quantity,
                classification = fromDomain(item.classification),
                additionalClassifications = item.additionalClassifications?.map { fromDomain(it) },
                unit = fromDomain(item.unit),
                relatedLot = item.relatedLot
            )

        private fun fromDomain(classification: CNEntity.Tender.Item.Classification): Tender.Item.Classification =
            Tender.Item.Classification(
                id = classification.id,
                scheme = classification.scheme,
                description = classification.description
            )

        private fun fromDomain(classification: CNEntity.Tender.Item.AdditionalClassification): Tender.Item.Classification =
            Tender.Item.Classification(
                id = classification.id,
                scheme = classification.scheme,
                description = classification.description
            )

        private fun fromDomain(unit: CNEntity.Tender.Item.Unit): Tender.Item.Unit =
            Tender.Item.Unit(
                id = unit.id,
                name = unit.name
            )
    }
}

