package com.procurement.access.application.service.tender.strategy.prepare.cancellation

import com.procurement.access.model.dto.ocds.LotStatus
import com.procurement.access.model.dto.ocds.LotStatusDetails
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import java.util.*

data class PreparedCancellationData(
    val tender: Tender,
    val lots: List<Lot>
) {

    data class Tender(val statusDetails: TenderStatusDetails)

    data class Lot(
        val id: UUID,
        val status: LotStatus,
        val statusDetails: LotStatusDetails
    )
}
