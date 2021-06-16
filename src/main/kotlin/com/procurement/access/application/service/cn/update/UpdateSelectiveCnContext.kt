package com.procurement.access.application.service.cn.update

import com.procurement.access.domain.model.enums.ProcurementMethod
import java.time.LocalDateTime
import java.util.*

data class UpdateSelectiveCnContext(
    val cpid: String,
    val ocid: String,
    val token: UUID,
    val owner: String,
    val pmd: ProcurementMethod,
    val startDate: LocalDateTime,
    val isAuction: Boolean
)
