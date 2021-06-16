package com.procurement.access.application.model.context

import com.procurement.access.domain.model.enums.Stage

data class GetAwardCriteriaAndConversionsContext(
    val cpid: String,
    val ocid: String,
    val stage: Stage
)
