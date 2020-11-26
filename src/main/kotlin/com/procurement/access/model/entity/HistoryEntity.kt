package com.procurement.access.model.entity

import java.time.LocalDateTime

data class HistoryEntity(

    var operationId: String,

    var command: String,

    var operationDate: LocalDateTime,

    var jsonData: String
)


