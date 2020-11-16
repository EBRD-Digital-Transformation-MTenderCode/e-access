package com.procurement.access.infrastructure.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.MainProcurementCategory

data class TenderCategoryInfo(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @field:JsonProperty("mainProcurementCategory") @param:JsonProperty("mainProcurementCategory") val mainProcurementCategory: MainProcurementCategory
    )
}
