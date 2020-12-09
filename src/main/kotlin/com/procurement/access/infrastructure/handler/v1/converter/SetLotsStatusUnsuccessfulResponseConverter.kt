package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.lot.SettedLotsStatusUnsuccessful
import com.procurement.access.infrastructure.handler.v1.model.response.SetLotsStatusUnsuccessfulResponse

fun SettedLotsStatusUnsuccessful.convert() = SetLotsStatusUnsuccessfulResponse(
    tender = this.tender.let { tender ->
        SetLotsStatusUnsuccessfulResponse.Tender(
            status = tender.status,
            statusDetails = tender.statusDetails
        )
    },
    lots = this.lots
        .map { lot ->
            SetLotsStatusUnsuccessfulResponse.Lot(
                id = lot.id,
                status = lot.status
            )
        }
)
