package com.procurement.access.application.service.tender.strategy.get.lots

import com.procurement.access.domain.model.lot.LotId

data class GetActiveLotsResult(
    val lots: List<Lot>
) {
    data class Lot(
        val id: LotId
    )
}