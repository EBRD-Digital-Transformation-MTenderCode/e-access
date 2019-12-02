package com.procurement.access.application.service.lot

import java.time.LocalDateTime

class SetLotsStatusUnsuccessfulContext(
    val cpid: String,
    val stage: String,
    val startDate: LocalDateTime
)
