package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.CheckEqualityCurrenciesParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.handler.check.currency.CheckEqualityCurrenciesRequest

fun CheckEqualityCurrenciesRequest.convert(): Result<CheckEqualityCurrenciesParams, DataErrors> = CheckEqualityCurrenciesParams.tryCreate(
    cpid = cpid,
    ocid = ocid,
    ocidAP = ocidAP,
    cpidAP = cpidAP
)
