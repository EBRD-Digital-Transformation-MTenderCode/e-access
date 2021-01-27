package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.tender.strategy.get.items.GetItemsByLotIdsParams
import com.procurement.access.infrastructure.handler.v2.model.request.GetItemsByLotIdsRequest

fun GetItemsByLotIdsRequest.convert() =
    GetItemsByLotIdsParams.tryCreate(cpid = cpid, ocid = ocid, tender = tender.convert())

fun GetItemsByLotIdsRequest.Tender.convert() =
    GetItemsByLotIdsParams.Tender(lots = lots.map { it.convert() })

fun GetItemsByLotIdsRequest.Tender.Lot.convert() =
    GetItemsByLotIdsParams.Tender.Lot(id = id)