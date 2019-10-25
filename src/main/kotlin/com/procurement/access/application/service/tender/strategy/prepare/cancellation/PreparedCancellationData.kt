package com.procurement.access.application.service.tender.strategy.prepare.cancellation

import com.procurement.access.domain.model.enums.TenderStatusDetails

data class PreparedCancellationData(
    val tender: Tender
) {

    data class Tender(val statusDetails: TenderStatusDetails)
}
