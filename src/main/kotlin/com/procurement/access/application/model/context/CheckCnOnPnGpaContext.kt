package com.procurement.access.application.model.context

import com.procurement.access.domain.model.enums.ProcurementMethod
import java.time.LocalDateTime

data class CheckCnOnPnGpaContext(
    val cpid: String,
    val previousStage: String,
    val country: String,
    val pmd: ProcurementMethod,
    val startDate: LocalDateTime
)
