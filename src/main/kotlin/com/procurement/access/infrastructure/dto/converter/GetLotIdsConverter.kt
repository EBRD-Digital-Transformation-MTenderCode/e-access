package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.lot.GetLotIdsParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Option
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.extension.mapResult
import com.procurement.access.infrastructure.handler.get.lotids.GetLotIdsRequest

fun GetLotIdsRequest.convert(): Result<GetLotIdsParams, List<DataErrors>> {

    val statesResult = this.states?.mapResult { it.convert() }

    if (statesResult != null && statesResult.isFail)
        return Result.failure(statesResult.error)

    val getLotIdsParamsResult = GetLotIdsParams.tryCreate(
        cpid = this.cpid,
        states = Option.fromNullable(statesResult?.get),
        ocid = this.ocid
    )
    if (getLotIdsParamsResult.isFail)
        return Result.failure(listOf(getLotIdsParamsResult.error))

    return Result.success(getLotIdsParamsResult.get)
}

fun GetLotIdsRequest.State.convert(): Result<GetLotIdsParams.State, List<DataErrors>> {
    val stateResult = GetLotIdsParams.State.tryCreate(status = this.status, statusDetails = this.statusDetails)
    if (stateResult.isFail)
        return Result.failure(listOf(stateResult.error))
    return Result.success(stateResult.get)
}
