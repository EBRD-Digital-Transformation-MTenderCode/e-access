package com.procurement.access.infrastructure.handler.check.currency

import com.fasterxml.jackson.annotation.JsonProperty

class CheckEqualityCurrenciesRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("relatedCpid") @param:JsonProperty("relatedCpid") val relatedCpid: String,
    @field:JsonProperty("relatedOcid") @param:JsonProperty("relatedOcid") val relatedOcid: String
)