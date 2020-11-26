package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.SetStateForLotsParams
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.lot.tryCreateLotId
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.handler.set.stateforlots.SetStateForLotsRequest
import com.procurement.access.infrastructure.handler.set.stateforlots.SetStateForLotsResult
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.extension.toSet
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess

fun SetStateForLotsRequest.convert(): Result<SetStateForLotsParams, DataErrors> {

    if (this.lots.isEmpty()) {
        return Result.failure(
            DataErrors.Validation.EmptyArray(name = "lots")
        )
    }
    val lotsId = this.lots
        .map { it.id }
    val uniqueLotsId = this.lots
        .toSet { it.id }
        .toList()
    val nonUniqueLotsId = lotsId - uniqueLotsId
    if (nonUniqueLotsId.isNotEmpty()) {
        return DataErrors.Validation.UniquenessDataMismatch(
            name = "lots",
            value = nonUniqueLotsId.joinToString { it }
        ).asFailure()
    }

    val lotsResult = this.lots
        .mapResult { it.convert() }
        .onFailure { return it }

    return SetStateForLotsParams.tryCreate(cpid = this.cpid, ocid = this.ocid, lots = lotsResult)
}

fun SetStateForLotsRequest.Lot.convert(): Result<SetStateForLotsParams.Lot, DataErrors> {
    return SetStateForLotsParams.Lot.tryCreate(id = this.id, status = this.status, statusDetails = this.statusDetails)
}

fun CNEntity.Tender.Lot.convertToSetStateForLotsResult():Result<SetStateForLotsResult, Fail.Incident.Parsing> =
    SetStateForLotsResult(
        id = id.tryCreateLotId()
            .onFailure { fail -> return fail },
        status = status,
        statusDetails = statusDetails
    ).asSuccess()

fun APEntity.Tender.Lot.convertToSetStateForLotsResult():Result<SetStateForLotsResult, Fail.Incident.Parsing> =
    SetStateForLotsResult(
        id = id.tryCreateLotId()
            .onFailure { fail -> return fail },
        status = status,
        statusDetails = statusDetails
    ).asSuccess()


fun PNEntity.Tender.Lot.convertToSetStateForLotsResult():Result<SetStateForLotsResult, Fail.Incident.Parsing> =
    SetStateForLotsResult(
        id = id.tryCreateLotId()
            .onFailure { fail -> return fail },
        status = status,
        statusDetails = statusDetails
    ).asSuccess()
