package com.procurement.access.application.model.params

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

data class GetBuyersOwnersParams(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage
)