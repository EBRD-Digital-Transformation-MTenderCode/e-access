package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.Ocid

data class GetActiveLotsContext(
    val cpid: String,
    val ocid: Ocid.SingleStage
)
