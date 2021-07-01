package com.procurement.access.application.service.tender.strategy.get.awardCriteria

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

data class GetAwardCriteriaContext(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage
)
