package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.LotStatus
import com.procurement.access.model.dto.ocds.LotStatusDetails
import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails

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