package com.procurement.access.infrastructure.handler.v1.model.request.criterion

import com.fasterxml.jackson.annotation.JsonProperty

data class CriterionClassificationRequest(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
    @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String
)
