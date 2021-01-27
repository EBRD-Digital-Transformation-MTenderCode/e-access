package com.procurement.access.infrastructure.handler.v1.model.response

import com.fasterxml.jackson.annotation.JsonProperty

class GetMainProcurementCategoryResponse(
    @field:JsonProperty("mainProcurementCategory") @param:JsonProperty("mainProcurementCategory") val mainProcurementCategory: String
)
