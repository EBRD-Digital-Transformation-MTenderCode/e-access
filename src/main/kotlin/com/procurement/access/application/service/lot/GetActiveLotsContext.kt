package com.procurement.access.application.service.lot

data class GetActiveLotsContext(
    val cpid: String,
    val stage: String
)
