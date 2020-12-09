package com.procurement.access.model.entity

import java.time.LocalDateTime
import java.util.*


data class TenderProcessEntity(

    var cpId: String,

    var token: UUID,

    var owner: String,

    var stage: String,

    var createdDate: LocalDateTime,

    var jsonData: String
)
