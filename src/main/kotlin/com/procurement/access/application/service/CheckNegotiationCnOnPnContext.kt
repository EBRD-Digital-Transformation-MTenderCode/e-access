package com.procurement.access.application.service

import java.time.LocalDateTime

data class CheckNegotiationCnOnPnContext(
    val cpid: String,
    val previousStage: String,
    val startDate: LocalDateTime
)
