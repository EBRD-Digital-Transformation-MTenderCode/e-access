package com.procurement.access.infrastructure.handler.get.criteria

import com.fasterxml.jackson.annotation.JsonProperty

data class GetQualificationCriteriaAndMethodRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String
)
