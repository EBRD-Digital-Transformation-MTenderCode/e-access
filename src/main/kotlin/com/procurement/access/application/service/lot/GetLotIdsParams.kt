package com.procurement.access.application.service.lot

import com.procurement.access.domain.EnumElementProvider.Companion.keysAsStrings
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asFailure

class GetLotIdsParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val states: List<State>
) {
    companion object {

        private val allowedLotStatuses = LotStatus.allowedElements
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

        private val allowedLotStatusDetails = LotStatusDetails.allowedElements
            .filter {
                when (it) {
                    LotStatusDetails.EMPTY,
                    LotStatusDetails.AWARDED -> true
                    LotStatusDetails.UNSUCCESSFUL,
                    LotStatusDetails.CANCELLED -> false
                }
            }

        fun tryCreate(
            cpid: String,
            ocid: String,
            states: List<State>?
        ): Result<GetLotIdsParams, DataErrors> {

            val cpidResult = parseCpid(value = cpid)
                .doOnError { error-> return error.asFailure() }
                .get

            val ocidResult = parseOcid(value = ocid)
                .doOnError { error-> return error.asFailure() }
                .get

            if (states != null && states.isEmpty()) {
                return Result.failure(DataErrors.Validation.EmptyArray("GetLotIdsParams.states"))
            }

            return Result.success(
                GetLotIdsParams(
                    cpid = cpidResult,
                    states = states ?: emptyList(),
                    ocid = ocidResult
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
                            ?.takeIf { status -> status in allowedLotStatuses }
                            ?: return Result.failure(
                                DataErrors.Validation.UnknownValue(
                                    name = "status",
                                    expectedValues = allowedLotStatuses.keysAsStrings(),
                                    actualValue = it
                                )
                            )
                    }

                val createdStatusDetail = statusDetails
                    ?.let {
                        LotStatusDetails.orNull(statusDetails)
                            ?.takeIf { statusDetails -> statusDetails in allowedLotStatusDetails }
                            ?: return Result.failure(
                                DataErrors.Validation.UnknownValue(
                                    name = "statusDetails",
                                    expectedValues = allowedLotStatusDetails.keysAsStrings(),
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
