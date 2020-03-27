package com.procurement.access.application.service.lot

import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.util.Result
import com.procurement.access.lib.toSetBy

class GetLotIdsParams private constructor(
    val cpid: String,
    val ocid: String,
    val states: List<State>
) {
    companion object {

        private val allowedLotStatuses = LotStatus.values()
            .filter {
                when (it) {
                    LotStatus.PLANNING,
                    LotStatus.ACTIVE,
                    LotStatus.COMPLETE,
                    LotStatus.UNSUCCESSFUL,
                    LotStatus.CANCELLED -> true
                    LotStatus.PLANNED -> false
                }
            }
            .toSetBy { it.key }

        private val allowedLotStatusDetails = LotStatusDetails.values()
            .filter {
                when (it) {
                    LotStatusDetails.EMPTY,
                    LotStatusDetails.AWARDED -> true
                    LotStatusDetails.UNSUCCESSFUL,
                    LotStatusDetails.CANCELLED -> false
                }
            }
            .toSetBy { it.key }

        fun tryCreate(
            cpid: String,
            ocid: String,
            states: List<State>?
        ): Result<GetLotIdsParams, DataErrors> {

            if (states != null && states.isEmpty()) {
                return Result.failure(DataErrors.Validation.EmptyArray("GetLotIdsParams.states"))
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

    class State private constructor(
        val status: LotStatus?,
        val statusDetails: LotStatusDetails?
    ) : Comparable<State> {

        companion object {
            fun tryCreate(
                status: String?,
                statusDetails: String?
            ): Result<State, DataErrors> {

                val createdStatus = status
                    ?.let {
                        LotStatus.orNull(it)
                            ?.takeIf { status -> status.key in allowedLotStatuses }
                            ?: return Result.failure(
                                DataErrors.Validation.UnknownValue(
                                    name = "status",
                                    expectedValues = allowedLotStatuses,
                                    actualValue = it
                                )
                            )
                    }

                val createdStatusDetail = statusDetails
                    ?.let {
                        LotStatusDetails.orNull(statusDetails)
                            ?.takeIf { statusDetails -> statusDetails.key in allowedLotStatusDetails }
                            ?: return Result.failure(
                                DataErrors.Validation.UnknownValue(
                                    name = "statusDetails",
                                    expectedValues = allowedLotStatusDetails,
                                    actualValue = it
                                )
                            )
                    }


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
                    thisStatus.key.compareTo(otherStatus.key)
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
                    thisStatusDetails.key.compareTo(otherStatusDetail.key)
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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as State

            if (status != other.status) return false
            if (statusDetails != other.statusDetails) return false

            return true
        }

        override fun hashCode(): Int {
            var result = status?.hashCode() ?: 0
            result = 31 * result + (statusDetails?.hashCode() ?: 0)
            return result
        }
    }
}
