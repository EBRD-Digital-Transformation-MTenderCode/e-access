package com.procurement.access.infrastructure.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.ProcurementMethodModalities

data class TenderProcurementMethodModalitiesInfo(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: TenderProcurementMethodModalities
) {
    data class TenderProcurementMethodModalities(
        @field:JsonProperty("procurementMethodModalities") @param:JsonProperty("procurementMethodModalities") val procurementMethodModalities: List<ProcurementMethodModalities>?
    )
}