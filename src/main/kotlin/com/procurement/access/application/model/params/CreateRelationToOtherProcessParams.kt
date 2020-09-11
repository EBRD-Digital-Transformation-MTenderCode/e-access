package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseEnum
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.success

class CreateRelationToOtherProcessParams(
    val cpid: Cpid,
    val ocid: String,
    val relatedCpid: Cpid,
    val operationType: OperationType
) {

    companion object {

        val allowedOperationType = OperationType.allowedElements
            .filter {
                when (it) {
                    OperationType.OUTSOURCING_PN,
                    OperationType.RELATION_AP -> true

                    OperationType.APPLY_QUALIFICATION_PROTOCOL,
                    OperationType.CREATE_CN,
                    OperationType.CREATE_CN_ON_PIN,
                    OperationType.CREATE_CN_ON_PN,
                    OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                    OperationType.CREATE_PIN,
                    OperationType.CREATE_PIN_ON_PN,
                    OperationType.CREATE_PN,
                    OperationType.CREATE_SUBMISSION,
                    OperationType.QUALIFICATION,
                    OperationType.QUALIFICATION_CONSIDERATION,
                    OperationType.QUALIFICATION_PROTOCOL,
                    OperationType.START_SECONDSTAGE,
                    OperationType.SUBMISSION_PERIOD_END,
                    OperationType.TENDER_PERIOD_END,
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
            operationType: String
        ): Result<CreateRelationToOtherProcessParams, DataErrors.Validation> {
            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { error -> return error }

            val parsedRelationCpid = parseCpid(value = relatedCpid)
                .orForwardFail { error -> return error }

            val parsedOperationType = parseEnum(
                value = operationType,
                target = OperationType,
                allowedEnums = allowedOperationType,
                attributeName = "operationType"
            )
                .orForwardFail { error -> return error }

            return success(
                CreateRelationToOtherProcessParams(
                    cpid = parsedCpid,
                    ocid = ocid,
                    relatedCpid = parsedRelationCpid,
                    operationType = parsedOperationType
                )
            )
        }
    }
}