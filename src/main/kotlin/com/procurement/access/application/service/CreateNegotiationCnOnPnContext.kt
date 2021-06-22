package com.procurement.access.application.service

import java.time.LocalDateTime

data class CreateNegotiationCnOnPnContext(
    val cpid: String,
    val ocid: String,
    val startDate: LocalDateTime
)
