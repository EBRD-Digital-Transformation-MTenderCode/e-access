package com.procurement.access.infrastructure.dto.converter.get.currency

import com.procurement.access.application.model.params.GetCurrencyParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.GetCurrencyRequest
import com.procurement.access.lib.functional.Result

fun GetCurrencyRequest.convert(): Result<GetCurrencyParams, DataErrors> =
    GetCurrencyParams.tryCreate(cpid = this.cpid, ocid = this.ocid)

