package com.procurement.access.application.service.ap.create

import com.procurement.access.application.model.Mode
import com.procurement.access.domain.model.enums.ProcurementMethod
import java.time.LocalDateTime

data class CreateApContext(
    val ocid: String,
    val owner: String,
    val country: String,
    val pmd: ProcurementMethod,
    val startDate: LocalDateTime,
    val mode: Mode
)
