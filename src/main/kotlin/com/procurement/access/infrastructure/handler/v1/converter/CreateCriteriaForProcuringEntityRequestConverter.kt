package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.criteria.CreateCriteriaForProcuringEntity.Params
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.CreateCriteriaForProcuringEntityRequest
import com.procurement.access.lib.functional.Result

fun CreateCriteriaForProcuringEntityRequest.convert(): Result<Params, DataErrors> {
    val convertedCriteria = this.criteria
        .map { it.convert() }

    return Params.tryCreate(
        cpid     = this.cpid,
        ocid     = this.ocid,
        date     = this.date,
        criteria = convertedCriteria,
        operationType = this.operationType
    )
}

fun CreateCriteriaForProcuringEntityRequest.Criterion.convert(): Params.Criterion {
    val requirementGroups = this.requirementGroups
        .map { it.convert() }

    return Params.Criterion(
        id                = this.id,
        title             = this.title,
        description       = this.description,
        classification    = this.classification.convert(),
        requirementGroups = requirementGroups
    )
}

fun CreateCriteriaForProcuringEntityRequest.Criterion.Classification.convert(): Params.Criterion.Classification =
    Params.Criterion.Classification(
        id     = this.id,
        scheme = this.scheme
    )

fun CreateCriteriaForProcuringEntityRequest.Criterion.RequirementGroup.convert(): Params.RequirementGroup {
    val convertedRequirements = this.requirements
        .map { it.convert() }

    return Params.RequirementGroup(
        id           = this.id,
        description  = this.description,
        requirements = convertedRequirements
    )
}

fun CreateCriteriaForProcuringEntityRequest.Criterion.RequirementGroup.Requirement.convert(): Params.Requirement =
    Params.Requirement(
        id          = this.id,
        title       = this.title,
        description = this.description
    )


