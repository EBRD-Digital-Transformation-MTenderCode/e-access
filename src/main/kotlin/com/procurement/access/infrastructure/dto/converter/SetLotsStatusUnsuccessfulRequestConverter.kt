package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.lot.SetLotsStatusUnsuccessfulData
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.lot.SetLotsStatusUnsuccessfulRequest
import com.procurement.access.lib.mapIfNotEmpty
import com.procurement.access.lib.orThrow

fun SetLotsStatusUnsuccessfulRequest.convert() = SetLotsStatusUnsuccessfulData(
    lots = this.lots
        .mapIfNotEmpty { lot ->
            SetLotsStatusUnsuccessfulData.Lot(
                id = lot.id
            )
        }
        .orThrow {
            ErrorException(
                error = ErrorType.IS_EMPTY,
                message = "The list of lots is empty."
            )
        }
)
