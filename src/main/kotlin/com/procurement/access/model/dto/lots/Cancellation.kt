package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CancellationRs @JsonCreator constructor(

        val lots: List<LotCancellation>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LotCancellation @JsonCreator constructor(

        val id: String,

        var status: LotStatus? = null,

        var statusDetails: LotStatusDetails?
)