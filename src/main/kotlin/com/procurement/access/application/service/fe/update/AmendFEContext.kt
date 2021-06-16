package com.procurement.access.application.service.fe.update

import java.time.LocalDateTime

data class AmendFEContext(
    val cpid: String,
    val ocid: String,
    val owner: String,
    val startDate: LocalDateTime
)
