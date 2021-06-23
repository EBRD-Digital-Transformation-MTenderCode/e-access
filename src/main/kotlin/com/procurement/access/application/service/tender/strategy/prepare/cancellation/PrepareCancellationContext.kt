package com.procurement.access.application.service.tender.strategy.prepare.cancellation

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import java.util.*

data class PrepareCancellationContext(
    val cpid: Cpid,
    val token: UUID,
    val owner: String,
    val ocid: Ocid.SingleStage
)
