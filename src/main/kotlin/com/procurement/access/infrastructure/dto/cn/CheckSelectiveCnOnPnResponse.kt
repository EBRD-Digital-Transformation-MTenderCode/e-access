package com.procurement.access.infrastructure.dto.cn

import com.fasterxml.jackson.annotation.JsonProperty

data class CheckSelectiveCnOnPnResponse(
    @field:JsonProperty("requireAuction") @param:JsonProperty("requireAuction") val requireAuction: Boolean
)
