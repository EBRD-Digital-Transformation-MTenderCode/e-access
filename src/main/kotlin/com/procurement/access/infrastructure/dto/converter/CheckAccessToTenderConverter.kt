package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.tender.strategy.check.CheckAccessToTenderParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.check.accesstotender.CheckAccessToTenderRequest
import com.procurement.access.lib.functional.Result

fun CheckAccessToTenderRequest.convert(): Result<CheckAccessToTenderParams, DataErrors> = CheckAccessToTenderParams.tryCreate(
    cpid = this.cpid,
    owner = this.owner,
    ocid = this.ocid,
    token = this.token
)
