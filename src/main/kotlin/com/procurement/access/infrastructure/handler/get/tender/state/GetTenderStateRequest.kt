package com.procurement.access.infrastructure.handler.get.tender.state


import com.fasterxml.jackson.annotation.JsonProperty

data class GetTenderStateRequest(
    @param:JsonProperty("cpid") @field:JsonProperty("cpid") val cpid: String,
    @param:JsonProperty("ocid") @field:JsonProperty("ocid") val ocid: String
)