package com.procurement.access.application.service.tender.strategy.prepare.cancellation

import com.procurement.access.model.dto.ocds.TenderStatusDetails

data class PreparedCancellationData(
    val tender: Tender
) {

    data class Tender(val statusDetails: TenderStatusDetails)
}
