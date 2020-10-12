package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.CheckEqualPNAndAPCurrencyParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.handler.check.currency.CheckEqualPNAndAPCurrencyRequest

fun CheckEqualPNAndAPCurrencyRequest.convert(): Result<CheckEqualPNAndAPCurrencyParams, DataErrors> = CheckEqualPNAndAPCurrencyParams.tryCreate(
    cpid = cpid,
    ocid = ocid,
    ocidAP = ocidAP,
    cpidAP = cpidAP
)
