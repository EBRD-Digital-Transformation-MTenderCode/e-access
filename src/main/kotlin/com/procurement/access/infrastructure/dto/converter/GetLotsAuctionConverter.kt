package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.data.GetLotsAuctionResponseData
import com.procurement.access.model.dto.response.GetLotsAuctionResponse

fun GetLotsAuctionResponseData.toResponseDto() : GetLotsAuctionResponse{
    return GetLotsAuctionResponse(
        tender = GetLotsAuctionResponse.Tender(
            id = this.tender.id,
            title = this.tender.title,
            description = this.tender.description,
            lots = this.tender.lots.map { lot ->
                GetLotsAuctionResponse.Tender.Lot(
                    id = lot.id,
                    title = lot.title,
                    description = lot.description,
                    value = lot.value
                )
            }
        )
    )
}
