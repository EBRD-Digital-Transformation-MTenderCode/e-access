package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful.SetTenderUnsuccessfulResult
import com.procurement.access.infrastructure.handler.v1.model.response.SetTenderUnsuccessfulResponse

fun SetTenderUnsuccessfulResult.convert() = SetTenderUnsuccessfulResponse(
    tender = this.tender.let { tender ->
        SetTenderUnsuccessfulResponse.Tender(
            status = tender.status,
            statusDetails = tender.statusDetails
        )
    },
    lots = this.lots
        .map { lot ->
            SetTenderUnsuccessfulResponse.Lot(
                id = lot.id,
                status = lot.status
            )
        }
)
