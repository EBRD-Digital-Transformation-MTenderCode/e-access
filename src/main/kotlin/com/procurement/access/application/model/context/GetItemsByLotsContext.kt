package com.procurement.access.application.model.context

data class GetItemsByLotsContext(
    val cpid: String,
    val stage: String
)