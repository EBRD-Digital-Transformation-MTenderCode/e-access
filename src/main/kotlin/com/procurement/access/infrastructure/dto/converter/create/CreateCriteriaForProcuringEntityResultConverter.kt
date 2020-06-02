package com.procurement.access.infrastructure.dto.converter.create

import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.create.CreateCriteriaForProcuringEntityResult.Criterion

fun CNEntity.Tender.Criteria.convertToResponse(): Criterion {
    val requirementGroups = this.requirementGroups
        .map { it.convertToResponse() }

    return Criterion(
        id                = this.id,
        title             = this.title,
        description       = this.description,
        source            = this.source!!,
        requirementGroups = requirementGroups
    )
}

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
        dataType    = this.dataType
    )


