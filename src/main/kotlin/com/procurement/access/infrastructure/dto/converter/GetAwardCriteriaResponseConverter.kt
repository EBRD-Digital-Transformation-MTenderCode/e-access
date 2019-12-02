package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.tender.strategy.get.awardCriteria.GetAwardCriteriaResult
import com.procurement.access.infrastructure.dto.tender.get.awardCriteria.GetAwardCriteriaResponse

fun GetAwardCriteriaResult.convert() =
    GetAwardCriteriaResponse(
        awardCriteria = this.awardCriteria
    )
