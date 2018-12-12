package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.LotStatus
import com.procurement.access.model.dto.ocds.LotStatusDetails
import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ActivationAcRq @JsonCreator constructor(

        val lotId: String,
        val stageEnd:Boolean
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ActivationAcRs @JsonCreator constructor(

    val tender:ActivationAcRsTender,
    val lot:ActivationAcRsLot
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ActivationAcRsTender @JsonCreator constructor(

    var status: TenderStatus,

    var statusDetails: TenderStatusDetails
)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ActivationAcRsLot @JsonCreator constructor(

        val id: String,

        var status: LotStatus? = null,

        var statusDetails: LotStatusDetails?
)