package com.procurement.access.application.service.lot

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.lot.tryCreateLotId
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess

class GetLotStateByIdsParams private constructor(
    val lotIds: List<LotId>,
    val cpid: Cpid,
    val ocid: Ocid
) {
    companion object {
        fun tryCreate(
            lotIds: List<String>,
            cpid: String,
            ocid: String
        ): Result<GetLotStateByIdsParams, DataErrors> {
            val cpidResult = parseCpid(value = cpid)
                .doOnError { error ->
                    return Result.failure(error)
                }
                .get
            val ocidResult = parseOcid(value = ocid)
                .doOnError { error ->
                    return Result.failure(error)
                }
                .get

            val lotIdsResult = if (lotIds.isNotEmpty()) {
                lotIds.map { lotId ->
                    lotId.tryCreateLotId()
                        .doOnError {
                            return DataErrors.Validation.DataFormatMismatch(
                                actualValue = lotId,
                                name = "lotIds",
                                expectedFormat = "uuid"
                            ).asFailure()
                        }
                        .get
                }
            } else {
                return DataErrors.Validation.EmptyArray(name = "lotIds")
                    .asFailure()
            }

            return GetLotStateByIdsParams(
                cpid = cpidResult,
                ocid = ocidResult,
                lotIds = lotIdsResult
            ).asSuccess()
        }
    }
}
