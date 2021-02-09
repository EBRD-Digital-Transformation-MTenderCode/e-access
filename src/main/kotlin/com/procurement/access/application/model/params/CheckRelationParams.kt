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
    val ocid: Ocid,
    val relatedCpid: Cpid,
    val operationType: OperationType,
    val existenceRelation: Boolean
) {
    companion object {
        private val allowedOperationType = OperationType.allowedElements
            .filter {
                when (it) {
                    OperationType.RELATION_AP -> true

                    OperationType.AMEND_FE,
                    OperationType.APPLY_QUALIFICATION_PROTOCOL,
                    OperationType.COMPLETE_QUALIFICATION,
                    OperationType.CREATE_AWARD,
                    OperationType.CREATE_CN,
                    OperationType.CREATE_CN_ON_PIN,
                    OperationType.CREATE_CN_ON_PN,
                    OperationType.CREATE_FE,
                    OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                    OperationType.CREATE_PCR,
                    OperationType.CREATE_PIN,
                    OperationType.CREATE_PIN_ON_PN,
                    OperationType.CREATE_PN,
                    OperationType.CREATE_SUBMISSION,
                    OperationType.DIVIDE_LOT,
                    OperationType.ISSUING_FRAMEWORK_CONTRACT,
                    OperationType.OUTSOURCING_PN,
                    OperationType.QUALIFICATION,
                    OperationType.QUALIFICATION_CONSIDERATION,
                    OperationType.QUALIFICATION_PROTOCOL,
                    OperationType.START_SECONDSTAGE,
                    OperationType.SUBMIT_BID,
                    OperationType.SUBMISSION_PERIOD_END,
                    OperationType.TENDER_PERIOD_END,
                    OperationType.UPDATE_AP,
                    OperationType.UPDATE_AWARD,
                    OperationType.UPDATE_CN,
                    OperationType.UPDATE_PN,
                    OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> false
                }
            }
            .toSet()

        fun tryCreate(
            cpid: String,
            ocid: String,
            relatedCpid: String,
            operationType: String,
            existenceRelation: Boolean
        ): Result<CheckRelationParams, DataErrors> {
            val cpidParsed = parseCpid(value = cpid)
                .onFailure { error -> return error }

            val ocidParsed = parseOcid(value = ocid)
                .onFailure { error -> return error }

            val relatedCpidParsed = parseCpid(value = relatedCpid)
                .onFailure { error -> return error }

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
                operationType = parsedOperationType,
                existenceRelation = existenceRelation
            ).asSuccess()
        }
    }
}