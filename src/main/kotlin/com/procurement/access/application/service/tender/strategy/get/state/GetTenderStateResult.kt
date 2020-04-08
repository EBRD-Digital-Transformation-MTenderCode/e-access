package com.procurement.access.application.service.tender.strategy.get.state

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails

data class GetTenderStateResult(
    @param:JsonProperty("status") @field:JsonProperty("status") val status: TenderStatus,
    @param:JsonProperty("statusDetails") @field:JsonProperty("statusDetails") val statusDetails: TenderStatusDetails
)