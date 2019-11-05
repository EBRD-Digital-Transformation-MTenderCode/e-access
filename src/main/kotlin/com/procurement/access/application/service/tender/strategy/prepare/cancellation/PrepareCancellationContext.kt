package com.procurement.access.application.service.tender.strategy.prepare.cancellation

import java.util.*

data class PrepareCancellationContext(
    val cpid: String,
    val token: UUID,
    val owner: String,
    val stage: String
)
