package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.data.GetLotsAuctionResponseData
import com.procurement.access.infrastructure.handler.v1.model.response.GetLotsAuctionResponse

fun GetLotsAuctionResponseData.toResponseDto(): GetLotsAuctionResponse {
    return GetLotsAuctionResponse(
        tender = GetLotsAuctionResponse.Tender(
            id = tender.id,
            title = tender.title,
            description = tender.description,
            value = tender.value?.let { value ->
                GetLotsAuctionResponse.Tender.Value(
                    currency = value.currency
                )
            },
            lots = tender.lots.map { lot ->
                GetLotsAuctionResponse.Tender.Lot(
                    id = lot.id,
                    title = lot.title,
                    description = lot.description,
                    value = lot.value?.let { value ->
                        GetLotsAuctionResponse.Tender.Lot.Value(
                            amount = value.amount,
                            currency = value.currency
                        )
                    }
                )
            }
        )
    )
}
