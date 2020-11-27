package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.params.CheckEqualityCurrenciesParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.CheckEqualityCurrenciesRequest
import com.procurement.access.lib.functional.Result

fun CheckEqualityCurrenciesRequest.convert(): Result<CheckEqualityCurrenciesParams, DataErrors> = CheckEqualityCurrenciesParams.tryCreate(
    cpid = cpid,
    ocid = ocid,
    relatedOcid = relatedOcid,
    relatedCpid = relatedCpid
)
