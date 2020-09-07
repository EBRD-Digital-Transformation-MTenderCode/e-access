package com.procurement.access.infrastructure.handler.check.fa

import com.fasterxml.jackson.annotation.JsonProperty

data class CheckExistenceFARequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String
)