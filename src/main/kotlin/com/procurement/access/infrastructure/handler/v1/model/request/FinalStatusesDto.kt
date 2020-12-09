package com.procurement.access.infrastructure.handler.v1.model.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails

data class FinalStatusesRq @JsonCreator constructor(

        val lotId: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FinalStatusesRs @JsonCreator constructor(

    val stageEnd: Boolean,

    val cpSuccess: Boolean,

    val tender: FinalTender?,

    val lots: List<FinalLot>
)

data class FinalTender @JsonCreator constructor(

        val id: String,

        val status: TenderStatus,

        val statusDetails: TenderStatusDetails
)

data class FinalLot @JsonCreator constructor(

        val id: String,

        var status: LotStatus,

        var statusDetails: LotStatusDetails
)