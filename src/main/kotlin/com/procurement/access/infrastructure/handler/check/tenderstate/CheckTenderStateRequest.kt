package com.procurement.access.infrastructure.handler.check.tenderstate


import com.fasterxml.jackson.annotation.JsonProperty

data class CheckTenderStateRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("pmd") @param:JsonProperty("pmd") val pmd: String,
    @field:JsonProperty("country") @param:JsonProperty("country") val country: String,
    @field:JsonProperty("operationType") @param:JsonProperty("operationType") val operationType: String
)