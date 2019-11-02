package com.procurement.access.application.service.tender.strategy.prepare.cancellation

import com.procurement.access.domain.model.enums.OperationType
import java.util.*

data class PrepareCancellationContext(
    val cpid: String,
    val token: UUID,
    val owner: String,
    val stage: String,
    val operationType: OperationType
)
