package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseEnum
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

class CheckTenderStateParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val pmd: ProcurementMethod,
    val country: String,
    val operationType: OperationType
) {
    companion object {

        private val allowedOperationType = OperationType.allowedElements
            .filter {
                when (it) {
                    OperationType.AMEND_FE,
                    OperationType.CREATE_CN,
                    OperationType.CREATE_CN_ON_PIN,
                    OperationType.CREATE_CN_ON_PN,
                    OperationType.CREATE_FE,
                    OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                    OperationType.CREATE_PIN,
                    OperationType.CREATE_PIN_ON_PN,
                    OperationType.CREATE_PN,
                    OperationType.CREATE_SUBMISSION,
                    OperationType.SUBMISSION_PERIOD_END,
                    OperationType.TENDER_PERIOD_END,
                    OperationType.UPDATE_AP,
                    OperationType.UPDATE_CN,
                    OperationType.UPDATE_PN -> false

                    OperationType.APPLY_QUALIFICATION_PROTOCOL,
                    OperationType.COMPLETE_QUALIFICATION,
                    OperationType.CREATE_PCR,
                    OperationType.OUTSOURCING_PN,
                    OperationType.QUALIFICATION,
                    OperationType.QUALIFICATION_CONSIDERATION,
                    OperationType.QUALIFICATION_PROTOCOL,
                    OperationType.RELATION_AP,
                    OperationType.START_SECONDSTAGE,
                    OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> true
                }
            }
            .toSet()

        fun tryCreate(
            cpid: String,
            ocid: String,
            pmd: String,
            country: String,
            operationType: String
        ): Result<CheckTenderStateParams, DataErrors> {
            val cpidParsed = parseCpid(value = cpid)
                .orForwardFail { error -> return error }

            val ocidParsed = parseOcid(value = ocid)
                .orForwardFail { error -> return error }

            val pmdParsed = ProcurementMethod.tryCreate(name = pmd)
                .orForwardFail { error -> return error }

            val operationTypeParsed = parseEnum(
                value = operationType,
                attributeName = "operationType",
                allowedEnums = allowedOperationType,
                target = OperationType
            )
                .orForwardFail { error -> return error }

            return CheckTenderStateParams(
                cpid = cpidParsed,
                ocid = ocidParsed,
                pmd = pmdParsed,
                operationType = operationTypeParsed,
                country = country
            )
                .asSuccess()
        }
    }
}
