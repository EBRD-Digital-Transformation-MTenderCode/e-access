package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.criteria.FindCriteria
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.FindCriteriaRequest
import com.procurement.access.lib.functional.Result

fun FindCriteriaRequest.convert(): Result<FindCriteria.Params, DataErrors> =
    FindCriteria.Params.tryCreate(cpid = this.cpid, ocid = this.ocid, sources = this.source)
