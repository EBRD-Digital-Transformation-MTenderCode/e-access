package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.enums.Stage

data class GetActiveLotsContext(
    val cpid: String,
    val ocid: String,
    val stage: Stage
)
