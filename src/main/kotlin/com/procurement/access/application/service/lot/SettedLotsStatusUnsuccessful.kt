package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId

data class SettedLotsStatusUnsuccessful(
    val tender: Tender,
    val lots: List<Lot>
) {
    data class Tender(
        val status: TenderStatus,
        val statusDetails: TenderStatusDetails
    )

    data class Lot(
        val id: LotId,
        val status: LotStatus
    )
}
