package com.procurement.access.application.model.context

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.ProcurementMethod
import java.time.LocalDateTime

data class CheckOpenCnOnPnContext(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val country: String,
    val pmd: ProcurementMethod,
    val startDate: LocalDateTime
)
