package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.Item
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.LotStatus
import com.procurement.access.model.dto.ocds.TenderStatus

data class UpdateLotsRq @JsonCreator constructor(

        val unsuccessfulLots: HashSet<LotDto>?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateLotsRs @JsonCreator constructor(

        val tenderStatus: LotStatus?,

        val lots: List<Lot>,

        val items: List<Item>?
)
