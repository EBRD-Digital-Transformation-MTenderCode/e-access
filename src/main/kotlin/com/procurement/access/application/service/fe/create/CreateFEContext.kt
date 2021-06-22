package com.procurement.access.application.service.fe.create

import java.time.LocalDateTime

data class CreateFEContext(
    val cpid: String,
    val ocid: String,
    val owner: String,
    val startDate: LocalDateTime
)
