package com.procurement.access.infrastructure.handler.check.currency

import com.fasterxml.jackson.annotation.JsonProperty

class CheckEqualPNAndAPCurrencyRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("cpidAP") @param:JsonProperty("cpidAP") val cpidAP: String,
    @field:JsonProperty("ocidAP") @param:JsonProperty("ocidAP") val ocidAP: String
)