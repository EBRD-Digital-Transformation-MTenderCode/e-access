package com.procurement.access.application.model.context

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OperationType
import java.time.LocalDateTime

data class CheckFEDataContext(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val operationType: OperationType,
    val startDate: LocalDateTime
)
