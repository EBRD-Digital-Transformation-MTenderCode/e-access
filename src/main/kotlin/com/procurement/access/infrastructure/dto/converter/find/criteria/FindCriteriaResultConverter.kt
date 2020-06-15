package com.procurement.access.infrastructure.dto.converter.find.criteria

import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.find.criteria.FindCriteriaResult

fun CNEntity.Tender.Criteria.convert(): FindCriteriaResult.Criterion {
    val requirementGroups = this.requirementGroups
        .map { requirementGroup -> requirementGroup.convert() }

    return FindCriteriaResult.Criterion(
        id          = this.id,
        source      = this.source!!,
        description = this.description,
        title       = this.title,
        relatesTo   = this.relatesTo,
        relatedItem = this.relatedItem,
        requirementGroups = requirementGroups
    )
}

private fun CNEntity.Tender.Criteria.RequirementGroup.convert(): FindCriteriaResult.Criterion.RequirementGroup =
    FindCriteriaResult.Criterion.RequirementGroup(
        id           = this.id,
        description  = this.description,
        requirements = this.requirements
    )