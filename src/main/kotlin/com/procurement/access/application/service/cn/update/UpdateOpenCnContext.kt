package com.procurement.access.application.service.cn.update

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.ProcurementMethod
import java.time.LocalDateTime
import java.util.*

data class UpdateOpenCnContext(
    val cpid: Cpid,
    val token: UUID,
    val ocid: Ocid.SingleStage,
    val owner: String,
    val pmd: ProcurementMethod,
    val startDate: LocalDateTime,
    val isAuction: Boolean
)
