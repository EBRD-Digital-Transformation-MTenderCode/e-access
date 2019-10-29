package com.procurement.access.application.service

import com.procurement.access.domain.model.enums.ProcurementMethod
import java.time.LocalDateTime

data class CreateCnOnPnContext(
    val cpid: String,
    val previousStage: String,
    val stage: String,
    val country: String,
    val pmd: ProcurementMethod,
    val startDate: LocalDateTime
)
