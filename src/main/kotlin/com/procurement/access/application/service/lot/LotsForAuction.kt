package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.lot.TemporalLotId
import java.math.BigDecimal

class LotsForAuction(
    val lots: List<Lot>
) {

    data class Lot(
        val id: TemporalLotId,
        val value: Value
    ) {

        data class Value(
            val amount: BigDecimal,
            val currency: String
        )
    }
}