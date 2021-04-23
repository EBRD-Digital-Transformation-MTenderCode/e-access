package com.procurement.access.application.model.data

import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.RfqEntity
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
    ) { companion object {}
        data class Classification(
             val id: String,
             val scheme: Scheme,
             val description: String
        )

        data class AdditionalClassification(
             val id: String,
             val scheme: Scheme,
             val description: String
        )

        data class Unit(
             val id: String,
             val name: String
        )
    }
}

fun GetItemsByLotsResult.Item.Companion.fromDomain(item: CNEntity.Tender.Item) =
    GetItemsByLotsResult.Item(
        id = item.id,
        description = item.description,
        internalId = item.internalId,
        classification = item.classification.let { classification ->
            GetItemsByLotsResult.Item.Classification(
                id = classification.id,
                description = classification.description,
                scheme = classification.scheme
            )
        },
        additionalClassifications = item.additionalClassifications
            ?.map { additionalClassification ->
                GetItemsByLotsResult.Item.AdditionalClassification(
                    id = additionalClassification.id,
                    scheme = additionalClassification.scheme,
                    description = additionalClassification.description
                )
            },
        quantity = item.quantity,
        relatedLot = item.relatedLot,
        unit = item.unit.let { unit ->
            GetItemsByLotsResult.Item.Unit(
                id = unit.id,
                name = unit.name
            )
        }
    )

fun GetItemsByLotsResult.Item.Companion.fromDomain(item: RfqEntity.Tender.Item) =
    GetItemsByLotsResult.Item(
        id = item.id,
        description = item.description,
        internalId = item.internalId,
        classification = item.classification.let { classification ->
            GetItemsByLotsResult.Item.Classification(
                id = classification.id,
                description = classification.description,
                scheme = Scheme.creator(classification.scheme)
            )
        },
        additionalClassifications = emptyList(),
        quantity = item.quantity,
        relatedLot = item.relatedLot.toString(),
        unit = item.unit.let { unit ->
            GetItemsByLotsResult.Item.Unit(
                id = unit.id,
                name = unit.name
            )
        }
    )