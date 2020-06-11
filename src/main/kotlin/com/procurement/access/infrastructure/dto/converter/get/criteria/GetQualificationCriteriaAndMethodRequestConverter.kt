package com.procurement.access.infrastructure.dto.converter.get.criteria

import com.procurement.access.application.model.criteria.GetQualificationCriteriaAndMethod
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.handler.get.criteria.GetQualificationCriteriaAndMethodRequest

fun GetQualificationCriteriaAndMethodRequest.convert(): Result<GetQualificationCriteriaAndMethod.Params, DataErrors> =
    GetQualificationCriteriaAndMethod.Params.tryCreate(cpid = this.cpid, ocid = this.ocid)

