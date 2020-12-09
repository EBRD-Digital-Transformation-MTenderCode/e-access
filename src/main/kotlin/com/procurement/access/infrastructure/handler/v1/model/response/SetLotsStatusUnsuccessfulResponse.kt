package com.procurement.access.infrastructure.handler.v1.model.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId

data class SetLotsStatusUnsuccessfulResponse(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("unsuccessfulLots") @param:JsonProperty("unsuccessfulLots") val lots: List<Lot>
) {
    data class Tender(
        @field:JsonProperty("status") @param:JsonProperty("status") val status: TenderStatus,
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: TenderStatusDetails
    )

    data class Lot(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: LotId,
        @field:JsonProperty("status") @param:JsonProperty("status") val status: LotStatus
    )
}
