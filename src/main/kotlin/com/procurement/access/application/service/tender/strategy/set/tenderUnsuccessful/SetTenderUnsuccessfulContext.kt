package com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful

import com.procurement.access.domain.model.Ocid
import java.time.LocalDateTime

data class SetTenderUnsuccessfulContext(
    val cpid: String,
    val ocid: Ocid.SingleStage,
    val startDate: LocalDateTime
)
