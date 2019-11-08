package com.procurement.access.infrastructure.dto.tender.get.awardCriteria

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.AwardCriteria

data class GetAwardCriteriaResponse(
    @field:JsonProperty("awardCriteria") @param:JsonProperty("awardCriteria") val awardCriteria: AwardCriteria
)
