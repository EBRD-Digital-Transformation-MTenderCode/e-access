package com.procurement.access.application.service.cn.update

import com.procurement.access.application.model.Mode
import com.procurement.access.domain.model.enums.ProcurementMethod
import java.time.LocalDateTime

data class CnCreateContext(
    val country: String,
    val pmd: ProcurementMethod,
    val owner: String,
    val stage: String,
    val startDate: LocalDateTime,
    val phase: String,
    val mode: Mode
)
