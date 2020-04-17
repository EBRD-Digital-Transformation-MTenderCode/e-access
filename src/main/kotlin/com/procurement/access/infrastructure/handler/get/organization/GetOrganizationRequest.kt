package com.procurement.access.infrastructure.handler.get.organization

import com.fasterxml.jackson.annotation.JsonProperty

data class GetOrganizationRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("role") @param:JsonProperty("role") val role: String
)
