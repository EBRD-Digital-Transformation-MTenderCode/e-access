package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.lot.LotId

import java.math.BigDecimal

data class LotsForAuctionData(
    val lots: List<Lot>
) {

    data class Lot(
        val id: LotId,
        val value: Value
    ) {

        data class Value(
            val amount: BigDecimal,
            val currency: String
        )
    }
}