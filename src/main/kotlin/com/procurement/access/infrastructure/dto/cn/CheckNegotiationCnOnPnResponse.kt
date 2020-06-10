package com.procurement.access.infrastructure.dto.cn

import com.fasterxml.jackson.annotation.JsonProperty

data class CheckNegotiationCnOnPnResponse(
    @field:JsonProperty("requireAuction") @param:JsonProperty("requireAuction") val requireAuction: Boolean
)
