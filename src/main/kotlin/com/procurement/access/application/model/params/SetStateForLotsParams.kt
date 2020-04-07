package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseLotId
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.EnumElementProvider.Companion.keysAsStrings
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asFailure
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.lib.toSetBy

data class SetStateForLotsParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val lots: List<Lot>
) {
    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            lots: List<Lot>
        ): Result<SetStateForLotsParams, DataErrors> {
            val cpidResult = parseCpid(value = cpid)
                .doOnError { error -> return error.asFailure() }
                .get

            val ocidResult = parseOcid(value = ocid)
                .doOnError { error -> return error.asFailure() }
                .get


            return SetStateForLotsParams(cpid = cpidResult, ocid = ocidResult, lots = lots)
                .asSuccess()
        }
    }

    data class Lot private constructor(
        val id: LotId,
        val status: LotStatus,
        val statusDetails: LotStatusDetails
    ) {
        companion object {
            private val allowedLotStatuses = LotStatus.allowedElements
                .filter {
                    when (it) {
                        LotStatus.CANCELLED,
                        LotStatus.COMPLETE,
                        LotStatus.ACTIVE -> true
                        LotStatus.PLANNING,
                        LotStatus.PLANNED,
                        LotStatus.UNSUCCESSFUL -> false
                    }
                }
                .toSetBy { it }

            private val allowedLotStatusDetails = LotStatusDetails.allowedElements
                .filter {
                    when (it) {
                        LotStatusDetails.EMPTY -> true
                        LotStatusDetails.UNSUCCESSFUL,
                        LotStatusDetails.AWARDED,
                        LotStatusDetails.CANCELLED -> false
                    }
                }
                .toSetBy { it }

            fun tryCreate(
                id: String,
                status: String,
                statusDetails: String
            ): Result<Lot, DataErrors> {
                val idResult = parseLotId(value = id, attributeName = "Lot.id")
                    .doOnError { error -> return error.asFailure() }
                    .get

                val statusResult = LotStatus.orNull(key = status)
                    ?.takeIf { it in allowedLotStatuses }
                    ?: return Result.failure(
                        DataErrors.Validation.UnknownValue(
                            name = "Lot.status",
                            expectedValues = allowedLotStatuses.keysAsStrings(),
                            actualValue = status
                        )
                    )
                val statusDetailsResult = LotStatusDetails.orNull(key = statusDetails)
                    ?.takeIf { it in allowedLotStatusDetails }
                    ?: return Result.failure(
                        DataErrors.Validation.UnknownValue(
                            name = "Lot.statusDetails",
                            expectedValues = allowedLotStatusDetails.keysAsStrings(),
                            actualValue = statusDetails
                        )
                    )
                return Lot(id = idResult, status = statusResult, statusDetails = statusDetailsResult)
                    .asSuccess()
            }
        }
    }
}
