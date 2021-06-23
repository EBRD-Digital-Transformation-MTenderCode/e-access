package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.params.CheckExistenceFAParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.CheckExistenceFARequest
import com.procurement.access.lib.functional.Result

fun CheckExistenceFARequest.convert(): Result<CheckExistenceFAParams, DataErrors> =
    CheckExistenceFAParams.tryCreate(cpid, ocid)
