package com.procurement.access.application.model.context

import com.procurement.access.domain.model.Ocid

data class GetAwardCriteriaAndConversionsContext(
    val cpid: String,
    val ocid: Ocid.SingleStage
)
