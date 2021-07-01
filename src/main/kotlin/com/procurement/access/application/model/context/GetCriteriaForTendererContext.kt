package com.procurement.access.application.model.context

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

data class GetCriteriaForTendererContext(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage
)
