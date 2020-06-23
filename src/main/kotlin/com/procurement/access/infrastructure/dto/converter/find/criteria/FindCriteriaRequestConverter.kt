package com.procurement.access.infrastructure.dto.converter.find.criteria

import com.procurement.access.application.model.criteria.FindCriteria
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.handler.find.criteria.FindCriteriaRequest

fun FindCriteriaRequest.convert(): Result<FindCriteria.Params, DataErrors> =
    FindCriteria.Params.tryCreate(cpid = this.cpid, ocid = this.ocid, source = this.source)

