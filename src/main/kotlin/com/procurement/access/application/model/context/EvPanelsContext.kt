package com.procurement.access.application.model.context

import com.procurement.access.domain.model.Ocid
import java.time.LocalDateTime

data class EvPanelsContext(
    val cpid: String,
    val ocid: Ocid.SingleStage,
    val owner: String,
    val startDate: LocalDateTime
)