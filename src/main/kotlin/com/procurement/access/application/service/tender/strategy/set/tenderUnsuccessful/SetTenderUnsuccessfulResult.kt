package com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful

import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId

data class SetTenderUnsuccessfulResult(
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
