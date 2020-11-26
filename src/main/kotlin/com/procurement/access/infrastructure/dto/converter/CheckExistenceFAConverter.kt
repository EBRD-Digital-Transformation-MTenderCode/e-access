package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.CheckExistenceFAParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.check.fa.CheckExistenceFARequest
import com.procurement.access.lib.functional.Result

fun CheckExistenceFARequest.convert(): Result<CheckExistenceFAParams, DataErrors> =
    CheckExistenceFAParams.tryCreate(this.cpid)
