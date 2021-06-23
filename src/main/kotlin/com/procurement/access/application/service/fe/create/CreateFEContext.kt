package com.procurement.access.application.service.fe.create

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import java.time.LocalDateTime

data class CreateFEContext(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val owner: String,
    val startDate: LocalDateTime
)
