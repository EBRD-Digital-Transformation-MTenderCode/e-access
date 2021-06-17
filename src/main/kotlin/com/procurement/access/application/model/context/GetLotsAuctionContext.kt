package com.procurement.access.application.model.context

import com.procurement.access.domain.model.enums.Stage

data class GetLotsAuctionContext(
    val cpid: String,
    val ocid: String,
    val stage: Stage
)