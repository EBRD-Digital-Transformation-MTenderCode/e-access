package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.SetStateForTenderParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.set.statefortender.SetStateForTenderRequest
import com.procurement.access.lib.functional.Result

fun SetStateForTenderRequest.convert(): Result<SetStateForTenderParams, DataErrors> {

    val tender = this.tender
        .convert()
        .onFailure { return it }
    return SetStateForTenderParams.tryCreate(cpid = this.cpid, ocid = this.ocid, tender = tender)
}

fun SetStateForTenderRequest.Tender.convert(): Result<SetStateForTenderParams.Tender, DataErrors> {
    return SetStateForTenderParams.Tender.tryCreate(status = this.status, statusDetails = this.statusDetails)
}
