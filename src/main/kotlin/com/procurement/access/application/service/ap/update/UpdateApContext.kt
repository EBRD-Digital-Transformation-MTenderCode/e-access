package com.procurement.access.application.service.ap.update

import java.time.LocalDateTime
import java.util.*

data class UpdateApContext(
    val cpid: String,
    val token: UUID,
    val ocid: String,
    val owner: String,
    val startDate: LocalDateTime
)
