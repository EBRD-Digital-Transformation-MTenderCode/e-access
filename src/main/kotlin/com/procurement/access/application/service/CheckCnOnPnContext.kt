package com.procurement.access.application.service

import com.procurement.access.model.dto.ocds.ProcurementMethod
import java.time.LocalDateTime

data class CheckCnOnPnContext(
    val cpid: String,
    val previousStage: String,
    val country: String,
    val pmd: ProcurementMethod,
    val startDate: LocalDateTime
)
