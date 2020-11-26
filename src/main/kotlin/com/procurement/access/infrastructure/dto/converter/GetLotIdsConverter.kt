package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.lot.FindLotIdsParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.get.lotids.FindLotIdsRequest
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result

fun FindLotIdsRequest.convert(): Result<FindLotIdsParams, DataErrors> {

    val states = this.states
        ?.mapResult { it.convert() }
        ?.doOnError { error -> return Result.failure(error) }
        ?.get

    return FindLotIdsParams.tryCreate(cpid = this.cpid, ocid = this.ocid, states = states)

}

fun FindLotIdsRequest.State.convert(): Result<FindLotIdsParams.State, DataErrors> =
    FindLotIdsParams.State.tryCreate(status = this.status, statusDetails = this.statusDetails)

