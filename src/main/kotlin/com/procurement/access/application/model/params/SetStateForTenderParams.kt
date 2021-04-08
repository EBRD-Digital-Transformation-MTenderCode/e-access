package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.EnumElementProvider.Companion.keysAsStrings
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.lib.extension.toSet
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

class SetStateForTenderParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val tender: Tender
) {

    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            tender: Tender
        ): Result<SetStateForTenderParams, DataErrors> {
            val cpidResult = parseCpid(value = cpid)
                .onFailure { return it }

            val ocidResult = parseOcid(value = ocid)
                .onFailure { return it }

            return SetStateForTenderParams(cpid = cpidResult, ocid = ocidResult, tender = tender)
                .asSuccess()
        }
    }

    class Tender private constructor(
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
                        TenderStatus.PLANNING,
                        TenderStatus.UNSUCCESSFUL -> true
                        TenderStatus.PLANNED -> false
                    }
                }
                .toSet { it }

            private val allowedTenderStatusDetails = TenderStatusDetails.allowedElements
                .filter {
                    when (it) {
                        TenderStatusDetails.AGGREGATED,
                        TenderStatusDetails.AGGREGATION_PENDING,
                        TenderStatusDetails.AWARDING,
                        TenderStatusDetails.EMPTY,
                        TenderStatusDetails.EVALUATION,
                        TenderStatusDetails.LACK_OF_QUALIFICATIONS,
                        TenderStatusDetails.LACK_OF_SUBMISSIONS,
                        TenderStatusDetails.QUALIFICATION,
                        TenderStatusDetails.QUALIFICATION_STANDSTILL,
                        TenderStatusDetails.SUSPENDED,
                        TenderStatusDetails.TENDERING -> true

                        TenderStatusDetails.AGGREGATION,
                        TenderStatusDetails.AUCTION,
                        TenderStatusDetails.AWARDED_CONTRACT_PREPARATION,
                        TenderStatusDetails.AWARDED_STANDSTILL,
                        TenderStatusDetails.AWARDED_SUSPENDED,
                        TenderStatusDetails.CANCELLATION,
                        TenderStatusDetails.CLARIFICATION,
                        TenderStatusDetails.COMPLETE,
                        TenderStatusDetails.NEGOTIATION,
                        TenderStatusDetails.PLANNED,
                        TenderStatusDetails.PLANNING,
                        TenderStatusDetails.SUBMISSION -> false
                    }
                }
                .toSet { it }

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
                            actualValue = statusDetails
                        )
                    )
                return Tender(status = statusResult, statusDetails = statusDetailsResult)
                    .asSuccess()
            }
        }
    }
}
