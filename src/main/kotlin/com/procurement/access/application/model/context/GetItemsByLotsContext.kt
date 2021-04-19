package com.procurement.access.application.model.context

import com.procurement.access.domain.model.enums.Stage

data class GetItemsByLotsContext(
    val cpid: String,
    val stage: Stage
)