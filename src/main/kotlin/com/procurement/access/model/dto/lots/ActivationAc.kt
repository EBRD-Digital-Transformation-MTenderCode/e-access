package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.model.dto.ocds.LotStatusDetails

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ActivationAcRq @JsonCreator constructor(

        val relatedLots: List<String>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ActivationAcRs @JsonCreator constructor(

        val tender: ActivationAcTender,

        val lots: List<ActivationAcLot>,

        val stageEnd: Boolean
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ActivationAcTender @JsonCreator constructor(

        var status: TenderStatus,

        var statusDetails: TenderStatusDetails
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ActivationAcLot @JsonCreator constructor(

        val id: String,

        var status: LotStatus,

        var statusDetails: LotStatusDetails
)