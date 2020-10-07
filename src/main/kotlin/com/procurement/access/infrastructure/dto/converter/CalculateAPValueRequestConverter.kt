package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.CalculateAPValueParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.handler.calculate.value.CalculateAPValueRequest

fun CalculateAPValueRequest.convert(): Result<CalculateAPValueParams, DataErrors.Validation.DataMismatchToPattern> =
    CalculateAPValueParams.tryCreate(cpid = this.cpid, ocid = this.ocid)
