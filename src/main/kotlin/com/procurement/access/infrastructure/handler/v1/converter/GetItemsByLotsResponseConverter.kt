package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.data.GetItemsByLotsResult
import com.procurement.access.infrastructure.handler.v1.model.response.GetItemsByLotsResponse

fun GetItemsByLotsResult.convert() = GetItemsByLotsResponse(
    items = items.map { item ->
        GetItemsByLotsResponse.Item(
            id = item.id,
            description = item.description,
            internalId = item.internalId,
            classification = item.classification.let { classification ->
                GetItemsByLotsResponse.Item.Classification(
                    id = classification.id,
                    description = classification.description,
                    scheme = classification.scheme.toString()
                )
            },
            additionalClassifications = item.additionalClassifications
                ?.map { additionalClassification ->
                    GetItemsByLotsResponse.Item.AdditionalClassification(
                        id = additionalClassification.id,
                        scheme = additionalClassification.scheme.toString(),
                        description = additionalClassification.description
                    )
                },
            quantity = item.quantity,
            relatedLot = item.relatedLot,
            unit = item.unit.let { unit ->
                GetItemsByLotsResponse.Item.Unit(
                    id = unit.id,
                    name = unit.name
                )
            }
        )
    }
)