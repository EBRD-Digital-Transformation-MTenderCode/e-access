package com.procurement.access.application.model.params

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

data class GetMainProcurementCategoryParams(
    val cpid: Cpid,
    val ocid: Ocid
)
