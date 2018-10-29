package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.*

data class UpdateLotsRq @JsonCreator constructor(

        val unsuccessfulLots: HashSet<UpdateLotDto>?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateLotsRs @JsonCreator constructor(

        val tenderStatus: TenderStatus?,

        val tenderStatusDetails: TenderStatusDetails?,

        val lots: List<Lot>,

        val items: List<Item>?
)