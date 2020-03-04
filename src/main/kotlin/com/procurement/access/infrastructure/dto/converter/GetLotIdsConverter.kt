package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.lot.GetLotIdsParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.extension.mapResult
import com.procurement.access.infrastructure.handler.get.lotids.GetLotIdsRequest

fun GetLotIdsRequest.convert(): Result<GetLotIdsParams, List<DataErrors>> {

    val states = this.states
        ?.mapResult { it.convert() }
        ?.doOnError { error -> return Result.failure(error) }
        ?.get

    return GetLotIdsParams.tryCreate(cpid = this.cpid, ocid = this.ocid, states = states)
        .mapError { error ->
            listOf(error)
        }
}

fun GetLotIdsRequest.State.convert(): Result<GetLotIdsParams.State, List<DataErrors>> =
    GetLotIdsParams.State.tryCreate(status = this.status, statusDetails = this.statusDetails)
        .mapError { error ->
            listOf(error)
        }

