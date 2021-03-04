package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseEnum
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.success

class CreateRelationToOtherProcessParams(
    val cpid: Cpid,
    val ocid: String,
    val relatedCpid: Cpid,
    val relatedOcid: Ocid?,
    val operationType: OperationType
) {

    companion object {

        val allowedOperationType = OperationType.allowedElements
            .filter {
                when (it) {
                    OperationType.CREATE_PCR,
                    OperationType.OUTSOURCING_PN,
                    OperationType.RELATION_AP -> true

                    OperationType.AMEND_FE,
                    OperationType.APPLY_QUALIFICATION_PROTOCOL,
                    OperationType.COMPLETE_QUALIFICATION,
                    OperationType.CREATE_CN,
                    OperationType.CREATE_CN_ON_PIN,
                    OperationType.CREATE_CN_ON_PN,
                    OperationType.CREATE_FE,
                    OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                    OperationType.CREATE_PIN,
                    OperationType.CREATE_PIN_ON_PN,
                    OperationType.CREATE_PN,
                    OperationType.CREATE_SUBMISSION,
                    OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                    OperationType.DIVIDE_LOT,
                    OperationType.ISSUING_FRAMEWORK_CONTRACT,
                    OperationType.QUALIFICATION,
                    OperationType.QUALIFICATION_CONSIDERATION,
                    OperationType.QUALIFICATION_PROTOCOL,
                    OperationType.START_SECONDSTAGE,
                    OperationType.SUBMISSION_PERIOD_END,
                    OperationType.SUBMIT_BID,
                    OperationType.TENDER_PERIOD_END,
                    OperationType.UPDATE_AP,
                    OperationType.UPDATE_AWARD,
                    OperationType.UPDATE_CN,
                    OperationType.UPDATE_PN,
                    OperationType.WITHDRAW_QUALIFICATION_PROTOCOL,
                    OperationType.CREATE_AWARD-> false
                }
            }
            .toSet()

        fun tryCreate(
            cpid: String,
            ocid: String,
            relatedCpid: String,
            relatedOcid: String?,
            operationType: String
        ): Result<CreateRelationToOtherProcessParams, DataErrors.Validation> {
            val parsedCpid = parseCpid(value = cpid)
                .onFailure { error -> return error }

            val parsedRelationCpid = parseCpid(value = relatedCpid)
                .onFailure { error -> return error }

            val parsedRelationOcid = relatedOcid?.let {
                parseOcid(value = relatedOcid)
                    .onFailure { error -> return error }
            }

            val parsedOperationType = parseEnum(
                value = operationType,
                target = OperationType,
                allowedEnums = allowedOperationType,
                attributeName = "operationType"
            )
                .onFailure { error -> return error }

            return success(
                CreateRelationToOtherProcessParams(
                    cpid = parsedCpid,
                    ocid = ocid,
                    relatedCpid = parsedRelationCpid,
                    relatedOcid = parsedRelationOcid,
                    operationType = parsedOperationType
                )
            )
        }
    }
}