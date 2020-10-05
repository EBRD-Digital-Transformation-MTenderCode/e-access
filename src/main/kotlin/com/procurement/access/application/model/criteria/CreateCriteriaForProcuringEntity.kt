package com.procurement.access.application.model.criteria

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseEnum
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.util.Result

class CreateCriteriaForProcuringEntity {

    class Params private constructor(
        val cpid: Cpid,
        val ocid: Ocid,
        val criteria: List<Criterion>,
        val operationType: OperationType
    ) {
        companion object {

            val allowedOperationType = OperationType.allowedElements
                .filter {
                    when (it) {
                        OperationType.AMEND_FE,
                        OperationType.APPLY_QUALIFICATION_PROTOCOL,
                        OperationType.COMPLETE_QUALIFICATION,
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
                        OperationType.OUTSOURCING_PN,
                        OperationType.QUALIFICATION,
                        OperationType.QUALIFICATION_CONSIDERATION,
                        OperationType.QUALIFICATION_PROTOCOL,
                        OperationType.RELATION_AP,
                        OperationType.START_SECONDSTAGE,
                        OperationType.UPDATE_AP,
                        OperationType.UPDATE_CN,
                        OperationType.UPDATE_PN,
                        OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> false

                        OperationType.SUBMISSION_PERIOD_END,
                        OperationType.TENDER_PERIOD_END -> true
                    }
                }
                .toSet()

            fun tryCreate(
                cpid: String,
                ocid: String,
                criteria: List<Criterion>,
                operationType: String
            ): Result<Params, DataErrors> {

                val cpidResult = parseCpid(value = cpid)
                    .orForwardFail { error -> return error }

                val ocidResult = parseOcid(value = ocid)
                    .orForwardFail { error -> return error }

                val parsedOperationType = parseEnum(
                    value = operationType,
                    target = OperationType,
                    allowedEnums = allowedOperationType,
                    attributeName = "operationType"
                )
                    .orForwardFail { error -> return error }

                return Result.success(
                    Params(
                        cpid = cpidResult,
                        ocid = ocidResult,
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
            val requirementGroups: List<RequirementGroup>
        )

        class RequirementGroup(val id: String, val description: String?, val requirements: List<Requirement>)
        class Requirement(val id: String, val description: String?, val title: String)
    }
}
