package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.Ocid
import java.time.LocalDateTime

class SetLotsStatusUnsuccessfulContext(
    val cpid: String,
    val ocid: Ocid.SingleStage,
    val startDate: LocalDateTime
)
