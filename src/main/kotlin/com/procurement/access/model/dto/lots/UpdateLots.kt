package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.model.dto.ocds.Item
import com.procurement.access.model.dto.ocds.Lot

data class UpdateLotsRq @JsonCreator constructor(

    val unsuccessfulLots: List<UpdateLotDto>?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateLotsRs @JsonCreator constructor(

        val tenderStatus: TenderStatus?,

        val tenderStatusDetails: TenderStatusDetails?,

        val lots: List<Lot>,

        val items: List<Item>?
)