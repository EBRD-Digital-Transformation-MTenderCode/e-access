package com.procurement.access.infrastructure.handler.v2.model.request

import com.fasterxml.jackson.annotation.JsonProperty

data class CheckExistenceFARequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String
)