package com.procurement.access.infrastructure.dto.converter.get.criteria

import com.procurement.access.application.model.criteria.GetQualificationCriteriaAndMethod
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.GetQualificationCriteriaAndMethodRequest
import com.procurement.access.lib.functional.Result

fun GetQualificationCriteriaAndMethodRequest.convert(): Result<GetQualificationCriteriaAndMethod.Params, DataErrors> =
    GetQualificationCriteriaAndMethod.Params.tryCreate(cpid = this.cpid, ocid = this.ocid)

