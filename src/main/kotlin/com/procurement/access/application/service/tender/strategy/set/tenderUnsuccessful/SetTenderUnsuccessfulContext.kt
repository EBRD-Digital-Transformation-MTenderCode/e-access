package com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful

import java.time.LocalDateTime

data class SetTenderUnsuccessfulContext(
    val cpid: String,
    val stage: String,
    val startDate: LocalDateTime
)
