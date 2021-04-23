package com.procurement.access.infrastructure.handler.v1.model.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.infrastructure.entity.RfqEntity
import com.procurement.access.model.dto.ocds.Lot

data class CanCancellationRq @JsonCreator constructor(

    val lotId: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CanCancellationRs @JsonCreator constructor(

    val lot: CanCancellationLot
) { companion object {} }

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CanCancellationLot @JsonCreator constructor(

    val id: String,

    var status: LotStatus,

    var statusDetails: LotStatusDetails
)

fun CanCancellationRs.Companion.fromDomain(lot: Lot): CanCancellationRs =
    CanCancellationRs(
        lot = CanCancellationLot(
            id = lot.id,
            status = lot.status!!,
            statusDetails = lot.statusDetails!!
        )
    )

fun CanCancellationRs.Companion.fromDomain(lot: RfqEntity.Tender.Lot): CanCancellationRs =
    CanCancellationRs(
        lot = CanCancellationLot(
            id = lot.id.toString(),
            status = lot.status,
            statusDetails = lot.statusDetails
        )
    )