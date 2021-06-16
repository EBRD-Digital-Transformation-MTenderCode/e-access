package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.lot.LotId

data class GetLotContext(
    val cpid: String,
    val ocid: String,
    val lotId: LotId
)
