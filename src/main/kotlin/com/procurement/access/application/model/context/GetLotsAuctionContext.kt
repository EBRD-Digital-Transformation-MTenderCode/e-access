package com.procurement.access.application.model.context

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

data class GetLotsAuctionContext(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage
)