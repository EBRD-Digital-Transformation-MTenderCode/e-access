package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.lot.LotId

data class GetLotContext(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val lotId: LotId
)
