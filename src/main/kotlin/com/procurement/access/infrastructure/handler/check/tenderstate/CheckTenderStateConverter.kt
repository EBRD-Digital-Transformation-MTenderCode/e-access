package com.procurement.access.infrastructure.handler.check.tenderstate

import com.procurement.access.application.model.params.CheckTenderStateParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result

fun CheckTenderStateRequest.convert(): Result<CheckTenderStateParams, DataErrors> =
    CheckTenderStateParams.tryCreate(cpid, ocid, pmd, country, operationType)
