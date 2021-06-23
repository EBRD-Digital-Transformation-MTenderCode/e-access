package com.procurement.access.model.entity

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import java.time.LocalDateTime
import java.util.*


data class TenderProcessEntity(

    var cpId: Cpid,

    var token: UUID,

    var owner: String,

    var ocid: Ocid,

    var createdDate: LocalDateTime,

    var jsonData: String
)
