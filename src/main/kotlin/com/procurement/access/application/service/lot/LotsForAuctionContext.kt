package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OperationType

data class LotsForAuctionContext(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val operationType: OperationType
)
