package com.procurement.access.infrastructure.dto.tender.get.lots

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.lot.LotId

data class GetActiveLotsResponse(
    @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>
) {
    data class Lot(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: LotId
    )
}