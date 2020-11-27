package com.procurement.access.infrastructure.handler.v2.converter

import com.procurement.access.application.model.params.CheckTenderStateParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.CheckTenderStateRequest
import com.procurement.access.lib.functional.Result

fun CheckTenderStateRequest.convert(): Result<CheckTenderStateParams, DataErrors> =
    CheckTenderStateParams.tryCreate(cpid, ocid, pmd, country, operationType)
