package com.procurement.access.application.model.data

import java.math.BigDecimal

data class GetItemsByLotsResult(
     val items: List<Item>
) {
    data class Item(
        val id: String,
        val internalId: String?,
        val description: String,
        val quantity: BigDecimal,
        val classification: Classification,
        val additionalClassifications: List<AdditionalClassification>?,
        val unit: Unit,
        val relatedLot: String
    ) {
        data class Classification(
             val id: String,
             val scheme: String,
             val description: String
        )

        data class AdditionalClassification(
             val id: String,
             val scheme: String,
             val description: String
        )

        data class Unit(
             val id: String,
             val name: String
        )
    }
}