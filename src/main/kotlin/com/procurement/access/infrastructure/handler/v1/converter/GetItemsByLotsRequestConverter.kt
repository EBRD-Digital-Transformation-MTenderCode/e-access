package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.data.GetItemsByLotsData
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.handler.v1.model.request.GetItemsByLotsRequest

import com.procurement.access.lib.extension.mapIfNotEmpty
import com.procurement.access.lib.extension.orThrow

fun GetItemsByLotsRequest.convert() = GetItemsByLotsData(
    lots = lots.mapIfNotEmpty { lot ->
        GetItemsByLotsData.Lot(lot.id)
    }.orThrow {
        ErrorException(
            error = ErrorType.IS_EMPTY,
            message = "Lots are empty."
        )
    }
)