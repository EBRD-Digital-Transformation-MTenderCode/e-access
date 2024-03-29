package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseEnum
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

class CheckRelationParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val relatedCpid: Cpid,
    val relatedOcid: Ocid.SingleStage?,
    val operationType: OperationType,
    val existenceRelation: Boolean
) {
    companion object {
        private val allowedOperationType = OperationType.allowedElements
            .filter {
                when (it) {
                    OperationType.RELATION_AP,
                    OperationType.CREATE_RFQ -> true

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
                    OperationType.CREATE_SUBMISSION,
                    OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                    OperationType.DIVIDE_LOT,
                    OperationType.ISSUING_FRAMEWORK_CONTRACT,
                    OperationType.NEXT_STEP_AFTER_BUYERS_CONFIRMATION,
                    OperationType.NEXT_STEP_AFTER_INVITED_CANDIDATES_CONFIRMATION,
                    OperationType.OUTSOURCING_PN,
                    OperationType.QUALIFICATION,
                    OperationType.QUALIFICATION_CONSIDERATION,
                    OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
                    OperationType.QUALIFICATION_PROTOCOL,
                    OperationType.START_SECONDSTAGE,
                    OperationType.SUBMISSION_PERIOD_END,
                    OperationType.SUBMIT_BID,
                    OperationType.TENDER_PERIOD_END,
                    OperationType.UPDATE_AP,
                    OperationType.UPDATE_AWARD,
                    OperationType.UPDATE_CN,
                    OperationType.UPDATE_PN,
                    OperationType.WITHDRAW_BID,
                    OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> false
                }
            }
            .toSet()

        fun tryCreate(
            cpid: String,
            ocid: String,
            relatedCpid: String,
            relatedOcid: String?,
            operationType: String,
            existenceRelation: Boolean
        ): Result<CheckRelationParams, DataErrors> {
            val cpidParsed = parseCpid(value = cpid)
                .onFailure { error -> return error }

            val ocidParsed = parseOcid(value = ocid)
                .onFailure { error -> return error }

            val relatedCpidParsed = parseCpid(value = relatedCpid)
                .onFailure { error -> return error }

            val relatedOcidParsed = relatedOcid?.let {
                parseOcid(it).onFailure { error -> return error }
            }

            val parsedOperationType = parseEnum(
                value = operationType,
                target = OperationType,
                allowedEnums = allowedOperationType,
                attributeName = "operationType"
            )
                .onFailure { fail -> return fail }

            return CheckRelationParams(
                cpid = cpidParsed,
                ocid = ocidParsed,
                relatedCpid = relatedCpidParsed,
                relatedOcid = relatedOcidParsed,
                operationType = parsedOperationType,
                existenceRelation = existenceRelation
            ).asSuccess()
        }
    }
}