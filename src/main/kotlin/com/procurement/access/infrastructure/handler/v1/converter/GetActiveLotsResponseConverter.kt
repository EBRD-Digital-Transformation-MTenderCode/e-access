package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.tender.strategy.get.lots.GetActiveLotsResult
import com.procurement.access.infrastructure.handler.v1.model.response.GetActiveLotsResponse

fun GetActiveLotsResult.convert() =
    GetActiveLotsResponse(
        lots = this.lots.map { lot ->
            GetActiveLotsResponse.Lot(
                id = lot.id
            )
        }
    )
