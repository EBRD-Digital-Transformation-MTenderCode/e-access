package com.procurement.access.application.model.context

import com.procurement.access.domain.model.enums.ProcurementMethod
import java.time.LocalDateTime

data class CreateSelectiveCnOnPnContext(
    val cpid: String,
    val ocid: String,
    val stage: String,
    val country: String,
    val pmd: ProcurementMethod,
    val startDate: LocalDateTime
)
