package com.procurement.access.application.service.tender.strategy.get.awardCriteria

import com.procurement.access.domain.model.enums.Stage

data class GetAwardCriteriaContext(
    val cpid: String,
    val ocid: String,
    val stage: Stage
)
