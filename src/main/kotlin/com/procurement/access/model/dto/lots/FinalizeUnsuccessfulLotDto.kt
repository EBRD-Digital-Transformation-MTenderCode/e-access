package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.LotStatus
import com.procurement.access.model.dto.ocds.LotStatusDetails
import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails

data class FinalizeUnsuccessfulLotRq @JsonCreator constructor(

        val lotId: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FinalizeUnsuccessfulLotRs @JsonCreator constructor(

        val stageEnd: Boolean,

        val cpSuccess: Boolean,

        val tender: FinaleTender?,

        val lots: List<FinaleLot>
)

data class FinaleTender @JsonCreator constructor(

        val id: String,

        val status: TenderStatus,

        val statusDetails: TenderStatusDetails
)

data class FinaleLot @JsonCreator constructor(

        val id: String,

        var status: LotStatus,

        var statusDetails: LotStatusDetails
)