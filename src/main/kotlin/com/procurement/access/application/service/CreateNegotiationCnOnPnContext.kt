package com.procurement.access.application.service

import java.time.LocalDateTime

data class CreateNegotiationCnOnPnContext(
    val cpid: String,
    val previousStage: String,
    val stage: String,
    val startDate: LocalDateTime
)
