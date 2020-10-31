package com.procurement.access.infrastructure.handler.get.currency

import com.fasterxml.jackson.annotation.JsonProperty

data class GetCurrencyRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String
)
