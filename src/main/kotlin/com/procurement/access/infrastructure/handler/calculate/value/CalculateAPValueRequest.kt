package com.procurement.access.infrastructure.handler.calculate.value

import com.fasterxml.jackson.annotation.JsonProperty

class CalculateAPValueRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String
)
