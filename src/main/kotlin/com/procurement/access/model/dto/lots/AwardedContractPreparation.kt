package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.*

data class UpdateLotsEvRq @JsonCreator constructor(

        val unsuccessfulLots: HashSet<UpdateLotDto>?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateLotsEvRs @JsonCreator constructor(

        val tenderStatus: TenderStatus?,

        val tenderStatusDetails: TenderStatusDetails?,

        val mainProcurementCategory: MainProcurementCategory?,

        val lots: List<Lot>
)