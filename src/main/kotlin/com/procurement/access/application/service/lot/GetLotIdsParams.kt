package com.procurement.access.application.service.lot

import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
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
            states: List<State>?
        ): Result<GetLotIdsParams, DataErrors> {

            if (states != null && states.isEmpty()) {
                return Result.failure(DataErrors.EmptyArray("GetLotIdsParams.states"))
            }

            return Result.success(
                GetLotIdsParams(
                    cpid = cpid,
                    states = states ?: emptyList(),
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

                val createdStatus = status
                    ?.let { LotStatus.tryCreate(status) }
                    ?.doOnError {error ->  Result.failure(DataErrors.UnknownValue(error)) }
                    ?.get

                val createdStatusDetail =statusDetails
                    ?.let { LotStatusDetails.tryCreate(statusDetails) }
                    ?.doOnError {error ->  Result.failure(DataErrors.UnknownValue(error)) }
                    ?.get

                return Result.success(
                    State(
                        status = createdStatus,
                        statusDetails = createdStatusDetail
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
