package com.procurement.access.infrastructure.dto.converter.get.procurement

import com.procurement.access.application.model.params.GetMainProcurementCategoryParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.GetMainProcurementCategoryRequest
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

fun GetMainProcurementCategoryRequest.convert(): Result<GetMainProcurementCategoryParams, DataErrors> {
    val cpidParsed = parseCpid(cpid)
        .onFailure { return it }

    val ocidParsed = parseOcid(ocid)
        .onFailure { return it }

    return GetMainProcurementCategoryParams(
        cpid = cpidParsed, ocid = ocidParsed
    ).asSuccess()
}


