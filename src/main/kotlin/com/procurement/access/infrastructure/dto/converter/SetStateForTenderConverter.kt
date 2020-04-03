package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.SetStateForTenderParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asFailure
import com.procurement.access.infrastructure.handler.set.statefortender.SetStateForTenderRequest

fun SetStateForTenderRequest.convert(): Result<SetStateForTenderParams, DataErrors> {

    val tender = this.tender
        .convert()
        .doOnError { error -> return error.asFailure() }
        .get
    return SetStateForTenderParams.tryCreate(cpid = this.cpid, ocid = this.ocid, tender = tender)
}

fun SetStateForTenderRequest.Tender.convert(): Result<SetStateForTenderParams.Tender, DataErrors> {
    return SetStateForTenderParams.Tender.tryCreate(status = this.status, statusDetails = this.statusDetails)
}
