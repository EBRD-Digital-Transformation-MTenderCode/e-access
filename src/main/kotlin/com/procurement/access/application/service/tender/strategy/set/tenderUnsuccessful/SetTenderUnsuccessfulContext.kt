package com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful

import com.procurement.access.domain.model.enums.Stage
import java.time.LocalDateTime

data class SetTenderUnsuccessfulContext(
    val cpid: String,
    val stage: Stage,
    val startDate: LocalDateTime
)
