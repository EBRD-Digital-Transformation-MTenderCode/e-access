package com.procurement.access.infrastructure.handler.find.criteria

import com.fasterxml.jackson.annotation.JsonProperty

data class FindCriteriaRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("source") @param:JsonProperty("source") val source: String
)
