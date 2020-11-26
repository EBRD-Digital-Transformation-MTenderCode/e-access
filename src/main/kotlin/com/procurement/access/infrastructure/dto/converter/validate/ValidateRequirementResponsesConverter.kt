package com.procurement.access.infrastructure.dto.converter.validate

import com.procurement.access.application.service.requirement.ValidateRequirementResponsesParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.validate.ValidateRequirementResponsesRequest
import com.procurement.access.infrastructure.handler.validate.ValidateRequirementResponsesResult
import com.procurement.access.lib.functional.Result

fun ValidateRequirementResponsesRequest.convert(): Result<ValidateRequirementResponsesParams, DataErrors> {
    val requirementResponses = this.requirementResponses
        .map { requirementResponses ->
            requirementResponses.convert()
                .onFailure { error -> return error }
        }

    return ValidateRequirementResponsesParams.tryCreate(
        ocid = ocid,
        cpid = cpid,
        requirementResponses = requirementResponses,
        organizationIds = this.organizationIds,
        operationType = this.operationType
    )
}

fun ValidateRequirementResponsesRequest.RequirementResponse.convert():
    Result<ValidateRequirementResponsesParams.RequirementResponse, DataErrors> {
    val requirement = requirement.convert()
        .onFailure { error -> return error }

    val relatedCandidate = this.relatedCandidate.convert()

    return ValidateRequirementResponsesParams.RequirementResponse.tryCreate(
        id = id,
        value = value,
        requirement = requirement,
        relatedCandidate = relatedCandidate
    )
}

fun ValidateRequirementResponsesRequest.RequirementResponse.Requirement.convert():
    Result<ValidateRequirementResponsesParams.RequirementResponse.Requirement, DataErrors> =
    ValidateRequirementResponsesParams.RequirementResponse.Requirement.tryCreate(id = id)

fun ValidateRequirementResponsesRequest.RequirementResponse.RelatedCandidate.convert() =
    ValidateRequirementResponsesParams.RequirementResponse.RelatedCandidate(
        id = this.id,
        name = this.name
    )

fun List<ValidateRequirementResponsesParams.RequirementResponse>.convert(): ValidateRequirementResponsesResult {
    val requirementResponses = this.map { requirementResponse ->
        ValidateRequirementResponsesResult.RequirementResponse(
            id = requirementResponse.id,
            relatedCandidate = requirementResponse.relatedCandidate
                .let { candidate ->
                    ValidateRequirementResponsesResult.RequirementResponse.RelatedCandidate(
                        id = candidate.id,
                        name = candidate.name
                    )
                },
            value = requirementResponse.value,
            requirement = requirementResponse.requirement
                .let { requirement ->
                    ValidateRequirementResponsesResult.RequirementResponse.Requirement(
                        id = requirement.id
                    )
                }
        )
    }
    return ValidateRequirementResponsesResult(requirementResponses)
}