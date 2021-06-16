package com.procurement.access.application.model.context

import com.procurement.access.domain.model.enums.ProcurementMethod

data class CheckResponsesContext(
    val cpid: String,
    val ocid: String,
    val stage: String,
    val owner: String,
    val pmd: ProcurementMethod
)
