package com.procurement.access.application.service.ap.update

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import java.time.LocalDateTime
import java.util.*

data class UpdateApContext(
    val cpid: Cpid,
    val token: UUID,
    val ocid: Ocid.SingleStage,
    val owner: String,
    val startDate: LocalDateTime
)
