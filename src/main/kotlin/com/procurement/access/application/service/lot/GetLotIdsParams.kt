package com.procurement.access.application.service.lot

import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.util.Option
import com.procurement.access.domain.util.Result

data class GetLotIdsParams private constructor(
    val cpid: String,
    val ocid: String,
    val states: List<State>
) {
    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            states: Option<List<State>>
        ): Result<GetLotIdsParams, DataErrors> {

            if (states.isDefined && states.get.isEmpty()) {
                return Result.failure(DataErrors.EmptyArray("GetLotIdsParams.states"))
            }

            return Result.success(
                GetLotIdsParams(
                    cpid = cpid,
                    states = if (states.isDefined) states.get else emptyList(),
                    ocid = ocid
                )
            )
        }
    }

    data class State private constructor(
        val status: LotStatus?,
        val statusDetails: LotStatusDetails?
    ) : Comparable<State> {

        companion object {
            fun tryCreate(
                status: String?,
                statusDetails: String?
            ): Result<State, DataErrors> {
                val statusResult = if (status != null) LotStatus.tryCreate(status) else null

                if (statusResult != null && statusResult.isFail)
                    return Result.failure(DataErrors.UnknownValue(statusResult.error))

                val statusDetailResult =
                    if (statusDetails != null) LotStatusDetails.tryCreate(statusDetails) else null

                if (statusDetailResult != null && statusDetailResult.isFail)
                    return Result.failure(DataErrors.UnknownValue(statusDetailResult.error))

                return Result.success(
                    State(
                        status = statusResult?.get,
                        statusDetails = statusDetailResult?.get
                    )
                )
            }
        }

        override fun compareTo(other: State): Int {
            val result = compareStatus(status, other.status)
            return if (result == 0) {
                compareStatusDetails(statusDetails, other.statusDetails)
            } else
                result
        }

        private fun compareStatus(thisStatus: LotStatus?, otherStatus: LotStatus?): Int {
            return if (thisStatus != null) {
                if (otherStatus != null) {
                    thisStatus.value.compareTo(otherStatus.value)
                } else {
                    -1
                }
            } else {
                if (otherStatus != null) {
                    1
                } else {
                    0
                }
            }
        }

        private fun compareStatusDetails(
            thisStatusDetails: LotStatusDetails?,
            otherStatusDetail: LotStatusDetails?
        ): Int {
            return if (thisStatusDetails != null) {
                if (otherStatusDetail != null) {
                    thisStatusDetails.value.compareTo(otherStatusDetail.value)
                } else {
                    -1
                }
            } else {
                if (otherStatusDetail != null) {
                    1
                } else {
                    0
                }
            }
        }
    }
}
