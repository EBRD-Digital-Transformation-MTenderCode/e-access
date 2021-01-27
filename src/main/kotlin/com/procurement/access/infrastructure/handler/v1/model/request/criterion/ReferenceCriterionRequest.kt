package com.procurement.access.infrastructure.handler.v1.model.request.criterion

import com.fasterxml.jackson.annotation.JsonProperty

data class ReferenceCriterionRequest(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
    @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: CriterionClassificationRequest
)
