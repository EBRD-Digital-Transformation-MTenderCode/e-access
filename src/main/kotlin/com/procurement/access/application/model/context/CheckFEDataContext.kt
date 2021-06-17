package com.procurement.access.application.model.context

import com.procurement.access.domain.model.enums.OperationType
import java.time.LocalDateTime

data class CheckFEDataContext(
    val cpid: String,
    val ocid: String,
    val stage: String,
    val operationType: OperationType,
    val startDate: LocalDateTime
)
