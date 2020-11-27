package com.procurement.access.infrastructure.handler.v1.model.response

import com.fasterxml.jackson.annotation.JsonProperty

data class CheckOpenCnOnPnResponse(
    @field:JsonProperty("requireAuction") @param:JsonProperty("requireAuction") val requireAuction: Boolean
)
