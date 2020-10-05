package com.procurement.access.infrastructure.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails

data class TenderStateInfo(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: TenderState
) {
    data class TenderState(
        @field:JsonProperty("status") @param:JsonProperty("status") val status: TenderStatus,
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: TenderStatusDetails
    )
}