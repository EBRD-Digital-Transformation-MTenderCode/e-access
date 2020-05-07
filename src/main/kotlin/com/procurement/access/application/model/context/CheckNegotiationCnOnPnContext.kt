package com.procurement.access.application.model.context

import java.time.LocalDateTime

data class CheckNegotiationCnOnPnContext(
    val cpid: String,
    val previousStage: String,
    val startDate: LocalDateTime
)
