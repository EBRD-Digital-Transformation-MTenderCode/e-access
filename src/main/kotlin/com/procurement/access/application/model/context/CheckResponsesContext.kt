package com.procurement.access.application.model.context

data class CheckResponsesContext(
    val cpid: String,
    val stage: String,
    val owner: String
)
