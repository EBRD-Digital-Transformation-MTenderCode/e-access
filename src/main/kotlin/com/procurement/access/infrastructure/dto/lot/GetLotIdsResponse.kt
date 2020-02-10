package com.procurement.access.infrastructure.dto.lot

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.lot.LotId

data class GetLotIdsResponse(
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("result") @param:JsonProperty("result") val result: List<LotId>
)
