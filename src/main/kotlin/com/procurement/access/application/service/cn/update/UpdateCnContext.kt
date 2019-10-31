package com.procurement.access.application.service.cn.update

import com.procurement.access.domain.model.enums.ProcurementMethod
import java.time.LocalDateTime
import java.util.*

data class UpdateCnContext(
    val cpid: String,
    val token: UUID,
    val stage: String,
    val owner: String,
    val pmd: ProcurementMethod,
    val startDate: LocalDateTime,
    val isAuction: Boolean
)
