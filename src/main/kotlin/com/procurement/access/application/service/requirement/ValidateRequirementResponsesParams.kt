package com.procurement.access.application.service.requirement

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseEnum
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.organization.OrganizationId
import com.procurement.access.domain.model.requirement.RequirementId
import com.procurement.access.domain.model.requirement.response.RequirementResponseId
import com.procurement.access.domain.model.requirement.response.RequirementRsValue
import com.procurement.access.domain.model.requirement.tryToRequirementId
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

class ValidateRequirementResponsesParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val requirementResponses: List<RequirementResponse>,
    val organizationIds: List<OrganizationId>,
    val operationType: OperationType
) {
    companion object {
        private val allowedOperationTypes = OperationType.allowedElements
            .filter { value ->
                when (value) {
                    OperationType.CREATE_SUBMISSION           -> true

                    OperationType.AMEND_FE,
                    OperationType.APPLY_QUALIFICATION_PROTOCOL,
                    OperationType.AWARD_CONSIDERATION,
                    OperationType.COMPLETE_QUALIFICATION,
                    OperationType.CREATE_AWARD,
                    OperationType.CREATE_CN,
                    OperationType.CREATE_CN_ON_PIN,
                    OperationType.CREATE_CN_ON_PN,
                    OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
                    OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE,
                    OperationType.CREATE_FE,
                    OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                    OperationType.CREATE_PCR,
                    OperationType.CREATE_PIN,
                    OperationType.CREATE_PIN_ON_PN,
                    OperationType.CREATE_PN,
                    OperationType.CREATE_RFQ,
                    OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                    OperationType.DIVIDE_LOT,
                    OperationType.ISSUING_FRAMEWORK_CONTRACT,
                    OperationType.OUTSOURCING_PN,
                    OperationType.QUALIFICATION,
                    OperationType.QUALIFICATION_CONSIDERATION,
                    OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
                    OperationType.QUALIFICATION_PROTOCOL,
                    OperationType.RELATION_AP,
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
            }.toSet()

        fun tryCreate(
            cpid: String,
            ocid: String,
            requirementResponses: List<RequirementResponse>,
            organizationIds: List<OrganizationId>,
            operationType: String
        ): Result<ValidateRequirementResponsesParams, DataErrors> {
            val cpidParsed = parseCpid(value = cpid)
                .onFailure { error -> return error }

            val ocidParsed = parseOcid(value = ocid)
                .onFailure { error -> return error }

            val operationTypeParsed = parseEnum(
                value = operationType,
                attributeName = "operationType",
                allowedEnums = allowedOperationTypes,
                target = OperationType.Companion
            )
                .onFailure { error -> return error }

            return ValidateRequirementResponsesParams(
                cpid = cpidParsed,
                ocid = ocidParsed,
                requirementResponses = requirementResponses,
                organizationIds = organizationIds,
                operationType = operationTypeParsed
            ).asSuccess()
        }
    }

    class RequirementResponse private constructor(
        val id: RequirementResponseId,
        val value: RequirementRsValue,
        val requirement: Requirement,
        val relatedCandidate: RelatedCandidate
    ) {
        companion object {
            fun tryCreate(
                id: String,
                value: RequirementRsValue,
                requirement: Requirement,
                relatedCandidate: RelatedCandidate
            ): Result<RequirementResponse, DataErrors> {
                return RequirementResponse(
                    id = id,
                    value = value,
                    requirement = requirement,
                    relatedCandidate = relatedCandidate
                ).asSuccess()
            }
        }

        class Requirement private constructor(
            val id: RequirementId
        ) {
            companion object {
                fun tryCreate(id: String): Result<Requirement, DataErrors> {
                    val parsedId = id.tryToRequirementId()
                        .onFailure { error -> return error }
                    return Requirement(parsedId).asSuccess()
                }
            }
        }

        data class RelatedCandidate(
            val id: String,
            val name: String
        )
    }
}
