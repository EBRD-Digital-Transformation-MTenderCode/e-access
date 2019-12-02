package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.lot.LotId

class SetLotsStatusUnsuccessfulData(
    val lots: List<Lot>
) {
    data class Lot(
        val id: LotId
    )
}
