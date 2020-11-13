package com.procurement.access.infrastructure.dto.converter.get.procurement

import com.procurement.access.application.model.params.GetMainProcurementCategoryParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.infrastructure.handler.get.tender.procurement.GetMainProcurementCategoryRequest

fun GetMainProcurementCategoryRequest.convert(): Result<GetMainProcurementCategoryParams, DataErrors> {
    val cpidParsed = parseCpid(cpid)
        .orForwardFail { return it }

    val ocidParsed = parseOcid(ocid)
        .orForwardFail { return it }

    return GetMainProcurementCategoryParams(
        cpid = cpidParsed, ocid = ocidParsed
    ).asSuccess()
}


