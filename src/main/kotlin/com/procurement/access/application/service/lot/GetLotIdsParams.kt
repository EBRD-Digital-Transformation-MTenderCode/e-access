package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails

data class GetLotIdsParams(
    val cpid: String,
    val ocid: String,
    val states: List<State>
) {
    data class State(
        val status: LotStatus?,
        val statusDetails: LotStatusDetails?
    ) : Comparable<State> {

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
