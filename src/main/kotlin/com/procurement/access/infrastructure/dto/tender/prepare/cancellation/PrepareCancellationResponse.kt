package com.procurement.access.infrastructure.dto.tender.prepare.cancellation

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.LotStatus
import com.procurement.access.model.dto.ocds.LotStatusDetails
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import java.util.*

data class PrepareCancellationResponse(
    @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>,
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {

    data class Lot(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: UUID,
        @field:JsonProperty("status") @param:JsonProperty("status") val status: LotStatus,
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: LotStatusDetails
    )

    data class Tender(
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: TenderStatusDetails
    )
}
