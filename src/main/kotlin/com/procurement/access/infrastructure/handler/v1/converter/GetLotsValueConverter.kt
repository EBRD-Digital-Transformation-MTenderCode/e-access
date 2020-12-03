package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.params.GetLotsValueParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseLotId
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.GetLotsValueRequest
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

fun GetLotsValueRequest.convert() : Result<GetLotsValueParams, DataErrors> {
    val cpidParsed = parseCpid(cpid).onFailure { return it }
    val ocidParsed = parseOcid(ocid).onFailure { return it }
    val tender = tender.convert().onFailure { return it }

    return GetLotsValueParams(
        cpid = cpidParsed,
        ocid = ocidParsed,
        tender = tender
    ).asSuccess()
}


fun GetLotsValueRequest.Tender.convert(): Result<GetLotsValueParams.Tender, DataErrors> {
    val lots = lots.mapResult { parseLotId(it.id, "tender.lots.id") }
        .onFailure { return it }
        .map { lotId -> GetLotsValueParams.Tender.Lot(lotId) }

    return GetLotsValueParams.Tender(lots).asSuccess()
}