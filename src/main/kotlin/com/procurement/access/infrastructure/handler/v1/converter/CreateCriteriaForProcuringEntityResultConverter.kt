package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.handler.v2.model.response.CreateCriteriaForProcuringEntityResult.Criterion

fun CNEntity.Tender.Criteria.convertToResponse(): Criterion {
    val requirementGroups = this.requirementGroups
        .map { it.convertToResponse() }

    val classification = this.classification.convertToResponse()

    return Criterion(
        id                = this.id,
        title             = this.title,
        description       = this.description,
        source            = this.source!!,
        relatesTo         = this.relatesTo!!,
        classification    = classification,
        requirementGroups = requirementGroups
    )
}

fun CNEntity.Tender.Criteria.Classification.convertToResponse(): Criterion.Classification =
    Criterion.Classification(
        id      = this.id,
        scheme  = this.scheme
    )


fun CNEntity.Tender.Criteria.RequirementGroup.convertToResponse(): Criterion.RequirementGroup {
    val requirements = this.requirements
        .map { it.convertToResponse() }

    return Criterion.RequirementGroup(
        id           = this.id,
        description  = this.description,
        requirements = requirements
    )
}

fun Requirement.convertToResponse(): Criterion.RequirementGroup.Requirement =
    Criterion.RequirementGroup.Requirement(
        id          = this.id,
        description = this.description,
        title       = this.title,
        dataType    = this.dataType,
        datePublished = this.datePublished,
        status = this.status
    )

fun FEEntity.Tender.Criteria.convertToResponse(): Criterion {
    val requirementGroups = this.requirementGroups
        .map { it.convertToResponse() }

    val classification = this.classification?.convertToResponse()

    return Criterion(
        id                = this.id,
        title             = this.title,
        description       = this.description,
        source            = this.source,
        relatesTo         = this.relatesTo,
        classification    = classification,
        requirementGroups = requirementGroups
    )
}

fun FEEntity.Tender.Criteria.Classification.convertToResponse(): Criterion.Classification {
    return Criterion.Classification(
        id = this.id,
        scheme = this.scheme
    )
}

fun FEEntity.Tender.Criteria.RequirementGroup.convertToResponse(): Criterion.RequirementGroup {
    val requirements = this.requirements
        .map { it.convertToResponse() }

    return Criterion.RequirementGroup(
        id           = this.id,
        description  = this.description,
        requirements = requirements
    )
}

