package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.enums.Stage
import java.time.LocalDateTime

class SetLotsStatusUnsuccessfulContext(
    val cpid: String,
    val stage: Stage,
    val startDate: LocalDateTime
)
