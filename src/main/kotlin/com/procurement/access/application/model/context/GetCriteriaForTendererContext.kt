package com.procurement.access.application.model.context

import com.procurement.access.domain.model.enums.Stage

data class GetCriteriaForTendererContext(
    val cpid: String,
    val stage: Stage
)
