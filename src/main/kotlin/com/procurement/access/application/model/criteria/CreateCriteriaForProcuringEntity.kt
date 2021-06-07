package com.procurement.access.application.model.criteria

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseDate
import com.procurement.access.application.model.parseEnum
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.lib.functional.Result
import java.time.LocalDateTime

class CreateCriteriaForProcuringEntity {

    class Params private constructor(
        val cpid: Cpid,
        val ocid: Ocid.SingleStage,
        val date: LocalDateTime,
        val criteria: List<Criterion>,
        val operationType: OperationType
    ) {
        companion object {

            val allowedOperationType = OperationType.allowedElements
                .filter {
                    when (it) {
                        OperationType.AMEND_FE,
                        OperationType.APPLY_CONFIRMATIONS,
                        OperationType.APPLY_QUALIFICATION_PROTOCOL,
                        OperationType.AWARD_CONSIDERATION,
                        OperationType.COMPLETE_QUALIFICATION,
                        OperationType.CREATE_AWARD,
                        OperationType.CREATE_CN,
                        OperationType.CREATE_CN_ON_PIN,
                        OperationType.CREATE_CN_ON_PN,
                        OperationType.CREATE_CONTRACT,
                        OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
                        OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE,
                        OperationType.CREATE_FE,
                        OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                        OperationType.CREATE_PCR,
                        OperationType.CREATE_PIN,
                        OperationType.CREATE_PIN_ON_PN,
                        OperationType.CREATE_PN,
                        OperationType.CREATE_RFQ,
                        OperationType.CREATE_SUBMISSION,
                        OperationType.DIVIDE_LOT,
                        OperationType.ISSUING_FRAMEWORK_CONTRACT,
                        OperationType.NEXT_STEP_AFTER_BUYERS_CONFIRMATION,
                        OperationType.NEXT_STEP_AFTER_INVITED_CANDIDATES_CONFIRMATION,
                        OperationType.OUTSOURCING_PN,
                        OperationType.QUALIFICATION,
                        OperationType.QUALIFICATION_CONSIDERATION,
                        OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
                        OperationType.QUALIFICATION_PROTOCOL,
                        OperationType.RELATION_AP,
                        OperationType.START_SECONDSTAGE,
                        OperationType.SUBMIT_BID,
                        OperationType.UPDATE_AP,
                        OperationType.UPDATE_AWARD,
                        OperationType.UPDATE_CN,
                        OperationType.UPDATE_PN,
                        OperationType.WITHDRAW_BID,
                        OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> false

                        OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                        OperationType.SUBMISSION_PERIOD_END,
                        OperationType.TENDER_PERIOD_END -> true
                    }
                }
                .toSet()

            fun tryCreate(
                cpid: String,
                ocid: String,
                date: String,
                criteria: List<Criterion>,
                operationType: String
            ): Result<Params, DataErrors> {

                val cpidResult = parseCpid(value = cpid)
                    .onFailure { error -> return error }

                val ocidResult = parseOcid(value = ocid)
                    .onFailure { error -> return error }

                val parsedDate = parseDate(value = date, name = "date")
                    .onFailure { error -> return error }

                val parsedOperationType = parseEnum(
                    value = operationType,
                    target = OperationType,
                    allowedEnums = allowedOperationType,
                    attributeName = "operationType"
                )
                    .onFailure { error -> return error }

                return Result.success(
                    Params(
                        cpid = cpidResult,
                        ocid = ocidResult,
                        date = parsedDate,
                        criteria = criteria,
                        operationType = parsedOperationType
                    )
                )
            }
        }

        class Criterion(
            val id: String,
            val description: String?,
            val title: String,
            val classification: Classification,
            val requirementGroups: List<RequirementGroup>
        ) {
            data class Classification(
                val id: String,
                val scheme: String
            )
        }

        class RequirementGroup(val id: String, val description: String?, val requirements: List<Requirement>)
        class Requirement(val id: String, val description: String?, val title: String)
    }
}
