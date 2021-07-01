package com.procurement.access.application.model.context

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import java.time.LocalDateTime

data class CheckNegotiationCnOnPnContext(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val startDate: LocalDateTime
)
