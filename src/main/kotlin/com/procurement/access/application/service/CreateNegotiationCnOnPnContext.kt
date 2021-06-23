package com.procurement.access.application.service

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import java.time.LocalDateTime

data class CreateNegotiationCnOnPnContext(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val startDate: LocalDateTime
)
