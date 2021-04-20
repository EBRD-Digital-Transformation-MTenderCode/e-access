package com.procurement.access.application.model.context

import com.procurement.access.domain.model.enums.Stage
import java.time.LocalDateTime

data class EvPanelsContext(
    val cpid: String,
    val stage: Stage,
    val owner: String,
    val startDate: LocalDateTime
)