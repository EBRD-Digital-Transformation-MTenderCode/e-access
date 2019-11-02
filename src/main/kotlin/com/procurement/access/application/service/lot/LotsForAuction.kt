package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.lot.TemporalLotId
import com.procurement.access.domain.model.money.Money

class LotsForAuction(
    val lots: List<Lot>
) {

    data class Lot(
        val id: TemporalLotId,
        val value: Money
    )
}
