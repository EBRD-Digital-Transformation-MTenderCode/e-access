package com.procurement.access.infrastructure.dto.tender.prepare.cancellation

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.TenderStatusDetails

data class PrepareCancellationResponse(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {

    data class Tender(
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: TenderStatusDetails
    )
}
