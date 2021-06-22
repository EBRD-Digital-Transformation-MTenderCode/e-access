package com.procurement.access.application.service

import com.procurement.access.domain.model.enums.ProcurementMethod
import java.time.LocalDateTime

data class CreateOpenCnOnPnContext(
    val cpid: String,
    val ocid: String,
    val country: String,
    val pmd: ProcurementMethod,
    val startDate: LocalDateTime
)
