package com.procurement.access.infrastructure.handler.v1.model.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails

data class CanCancellationRq @JsonCreator constructor(

        val lotId: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CanCancellationRs @JsonCreator constructor(

        val lot: CanCancellationLot
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CanCancellationLot @JsonCreator constructor(

        val id: String,

        var status: LotStatus,

        var statusDetails: LotStatusDetails
)