package com.procurement.access.application.service.lot

import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.lot.tryCreateLotId
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asFailure
import com.procurement.access.domain.util.asSuccess

class GetLotStateByIdsParams private constructor(
    val lotIds: List<LotId>,
    val cpid: Cpid,
    val ocid: Ocid
) {
    companion object {
        fun tryCreate(
            lotIds: List<String>?,
            cpid: String,
            ocid: String
        ): Result<GetLotStateByIdsParams, DataErrors> {
            val cpidResult = Cpid.tryCreate(value = cpid)
                .doOnError { pattern ->
                    return DataErrors.Validation.DataMismatchToPattern(
                        actualValue = cpid,
                        name = "cpid",
                        pattern = pattern
                    ).asFailure()
                }
                .get
            val ocidResult = Ocid.tryCreate(value = ocid)
                .doOnError { pattern ->
                    return DataErrors.Validation.DataMismatchToPattern(
                        actualValue = ocid,
                        name = "ocid",
                        pattern = pattern
                    ).asFailure()
                }
                .get

            val lotIdsResult = lotIds?.map { lotId ->
                lotId.tryCreateLotId()
                    .doOnError { format ->
                        return DataErrors.Validation.DataFormatMismatch(
                            actualValue = lotId,
                            name = "lotIds",
                            expectedFormat = "uuid"
                        ).asFailure()
                    }
                    .get
            }
            return GetLotStateByIdsParams(
                cpid = cpidResult,
                ocid = ocidResult,
                lotIds = lotIdsResult ?: emptyList()
            ).asSuccess()
        }
    }
}
