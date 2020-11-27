package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.tender.strategy.get.awardCriteria.GetAwardCriteriaResult
import com.procurement.access.infrastructure.handler.v1.model.response.GetAwardCriteriaResponse

fun GetAwardCriteriaResult.convert() =
    GetAwardCriteriaResponse(
        awardCriteria = this.awardCriteria
    )
