package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.tender.strategy.get.lots.GetActiveLotsResult
import com.procurement.access.infrastructure.dto.tender.get.lots.GetActiveLotsResponse

fun GetActiveLotsResult.convert() =
    GetActiveLotsResponse(
        lots = this.lots.map { lot ->
            GetActiveLotsResponse.Lot(
                id = lot.id
            )
        }
    )
