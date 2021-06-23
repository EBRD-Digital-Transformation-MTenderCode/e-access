package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import java.time.LocalDateTime

class SetLotsStatusUnsuccessfulContext(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val startDate: LocalDateTime
)
