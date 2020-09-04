package com.procurement.access.infrastructure.handler.pn

import com.fasterxml.jackson.annotation.JsonProperty

class OutsourcingPNRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("cpidFA") @param:JsonProperty("cpidFA") val cpidFA: String
)
