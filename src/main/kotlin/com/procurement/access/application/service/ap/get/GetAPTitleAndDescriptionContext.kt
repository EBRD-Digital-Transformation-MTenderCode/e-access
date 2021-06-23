package com.procurement.access.application.service.ap.get

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

data class GetAPTitleAndDescriptionContext(
    val cpid: Cpid,
    val ocid: Ocid
)
