package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.EnumElementProvider.Companion.keysAsStrings
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asFailure
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.lib.toSetBy

data class SetStateForTenderParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val tender: Tender
) {

    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            tender: Tender
        ): Result<SetStateForTenderParams, DataErrors> {
            val cpidResult = parseCpid(value = cpid)
                .doOnError { error -> return error.asFailure() }
                .get

            val ocidResult = parseOcid(value = ocid)
                .doOnError { error -> return error.asFailure() }
                .get

            return SetStateForTenderParams(cpid = cpidResult, ocid = ocidResult, tender = tender)
                .asSuccess()
        }
    }

    data class Tender private constructor(
        val status: TenderStatus,
        val statusDetails: TenderStatusDetails
    ) {
        companion object {
            private val allowedTenderStatus = TenderStatus.allowedElements
                .filter {
                    when (it) {
                        TenderStatus.ACTIVE,
                        TenderStatus.CANCELLED,
                        TenderStatus.COMPLETE,
                        TenderStatus.UNSUCCESSFUL -> true
                        TenderStatus.PLANNED,
                        TenderStatus.PLANNING -> false
                    }
                }
                .toSetBy { it }

            private val allowedTenderStatusDetails = TenderStatusDetails.allowedElements
                .filter {
                    when (it) {
                        TenderStatusDetails.EMPTY,
                        TenderStatusDetails.SUSPENDED,
                        TenderStatusDetails.QUALIFICATION,
                        TenderStatusDetails.LACK_OF_SUBMISSIONS -> true
                        TenderStatusDetails.PLANNING,
                        TenderStatusDetails.PLANNED,
                        TenderStatusDetails.CLARIFICATION,
                        TenderStatusDetails.NEGOTIATION,
                        TenderStatusDetails.TENDERING,
                        TenderStatusDetails.CANCELLATION,
                        TenderStatusDetails.AWARDING,
                        TenderStatusDetails.AUCTION,
                        TenderStatusDetails.AWARDED_STANDSTILL,
                        TenderStatusDetails.AWARDED_SUSPENDED,
                        TenderStatusDetails.AWARDED_CONTRACT_PREPARATION,
                        TenderStatusDetails.SUBMISSION,
                        TenderStatusDetails.COMPLETE -> false
                    }
                }
                .toSetBy { it }

            fun tryCreate(
                status: String,
                statusDetails: String
            ): Result<Tender, DataErrors> {
                val statusResult = TenderStatus.orNull(key = status)
                    ?.takeIf { it in allowedTenderStatus }
                    ?: return Result.failure(
                        DataErrors.Validation.UnknownValue(
                            name = "Tender.status",
                            expectedValues = allowedTenderStatus.keysAsStrings(),
                            actualValue = status
                        )
                    )
                val statusDetailsResult = TenderStatusDetails.orNull(key = statusDetails)
                    ?.takeIf { it in allowedTenderStatusDetails }
                    ?: return Result.failure(
                        DataErrors.Validation.UnknownValue(
                            name = "Tender.statusDetails",
                            expectedValues = allowedTenderStatusDetails.keysAsStrings(),
                            actualValue = status
                        )
                    )
                return Tender(status = statusResult, statusDetails = statusDetailsResult)
                    .asSuccess()
            }
        }
    }
}
