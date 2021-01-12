package com.procurement.access.application.model.context

import java.time.LocalDateTime

data class EvPanelsContext(
    val cpid: String,
    val stage: String,
    val owner: String,
    val startDate: LocalDateTime
)