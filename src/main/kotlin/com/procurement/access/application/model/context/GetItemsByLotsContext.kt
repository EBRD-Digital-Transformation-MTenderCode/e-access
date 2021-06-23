package com.procurement.access.application.model.context

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

data class GetItemsByLotsContext(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage
)