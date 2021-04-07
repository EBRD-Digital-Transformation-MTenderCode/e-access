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
import com.procurement.access.lib.extension.toSet
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

class SetStateForLotsParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val lots: List<Lot>
) {
    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            lots: List<Lot>
        ): Result<SetStateForLotsParams, DataErrors> {
            val cpidResult = parseCpid(value = cpid)
                .onFailure { return it }

            val ocidResult = parseOcid(value = ocid)
                .onFailure { return it }


            return SetStateForLotsParams(cpid = cpidResult, ocid = ocidResult, lots = lots)
                .asSuccess()
        }
    }

    class Lot private constructor(
        val id: LotId,
        val status: LotStatus,
        val statusDetails: LotStatusDetails?
    ) {
        companion object {
            private val allowedLotStatuses = LotStatus.allowedElements
                .filter {
                    when (it) {
                        LotStatus.CANCELLED,
                        LotStatus.COMPLETE,
                        LotStatus.ACTIVE,
                        LotStatus.UNSUCCESSFUL -> true
                        LotStatus.PLANNING,
                        LotStatus.PLANNED -> false
                    }
                }
                .toSet { it }

            private val allowedLotStatusDetails = LotStatusDetails.allowedElements
                .filter {
                    when (it) {
                        LotStatusDetails.EMPTY -> true
                        LotStatusDetails.UNSUCCESSFUL,
                        LotStatusDetails.AWARDED,
                        LotStatusDetails.CANCELLED -> false
                    }
                }
                .toSet { it }

            fun tryCreate(
                id: String,
                status: String,
                statusDetails: String?
            ): Result<Lot, DataErrors> {
                val idResult = parseLotId(value = id, attributeName = "Lot.id")
                    .onFailure { return it }

                val statusResult = LotStatus.orNull(key = status)
                    ?.takeIf { it in allowedLotStatuses }
                    ?: return Result.failure(
                        DataErrors.Validation.UnknownValue(
                            name = "Lot.status",
                            expectedValues = allowedLotStatuses.keysAsStrings(),
                            actualValue = status
                        )
                    )
                val statusDetailsResult =  statusDetails?.let {
                    LotStatusDetails.orNull(key = statusDetails)
                        ?.takeIf { it in allowedLotStatusDetails }
                        ?: return Result.failure(
                            DataErrors.Validation.UnknownValue(
                                name = "Lot.statusDetails",
                                expectedValues = allowedLotStatusDetails.keysAsStrings(),
                                actualValue = statusDetails
                            )
                        )
                }


                return Lot(id = idResult, status = statusResult, statusDetails = statusDetailsResult)
                    .asSuccess()
            }
        }
    }
}
