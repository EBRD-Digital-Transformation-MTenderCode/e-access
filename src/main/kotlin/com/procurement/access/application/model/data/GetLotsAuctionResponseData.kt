package com.procurement.access.application.model.data

import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.money.Money

data class GetLotsAuctionResponseData(
    val tender: Tender
) {
    data class Tender(
        val id: String,
        val title: String,
        val description: String,
        val lots: List<Lot>
    ) {
        data class Lot(
            val id: LotId,
            val title: String,
            val description: String,
            val value: Money
        )
    }
}
