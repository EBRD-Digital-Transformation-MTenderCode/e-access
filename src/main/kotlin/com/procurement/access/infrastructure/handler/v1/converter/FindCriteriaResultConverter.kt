package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.handler.v2.model.response.FindCriteriaResult

fun CNEntity.Tender.Criteria.convert(): FindCriteriaResult.Criterion {
    val requirementGroups = this.requirementGroups
        .map { requirementGroup -> requirementGroup.convert() }

    val classification = this.classification.convert()

    return FindCriteriaResult.Criterion(
        id = this.id,
        source = this.source!!,
        description = this.description,
        title = this.title,
        relatesTo = this.relatesTo,
        relatedItem = this.relatedItem,
        requirementGroups = requirementGroups,
        classification = classification
    )
}

private fun CNEntity.Tender.Criteria.Classification.convert(): FindCriteriaResult.Criterion.Classification =
    FindCriteriaResult.Criterion.Classification(
        id = this.id,
        scheme = this.scheme
    )

private fun CNEntity.Tender.Criteria.RequirementGroup.convert(): FindCriteriaResult.Criterion.RequirementGroup =
    FindCriteriaResult.Criterion.RequirementGroup(
        id = this.id,
        description = this.description,
        requirements = this.requirements
    )

fun FEEntity.Tender.Criteria.convert(): FindCriteriaResult.Criterion {
    val requirementGroups = this.requirementGroups
        .map { requirementGroup -> requirementGroup.convert() }

    val classification = this.classification?.convert()

    return FindCriteriaResult.Criterion(
        id = this.id,
        source = this.source,
        description = this.description,
        title = this.title,
        relatesTo = this.relatesTo,
        relatedItem = null,
        requirementGroups = requirementGroups,
        classification = classification
    )
}

private fun FEEntity.Tender.Criteria.Classification.convert(): FindCriteriaResult.Criterion.Classification =
    FindCriteriaResult.Criterion.Classification(
        id = this.id,
        scheme = this.scheme
    )

private fun FEEntity.Tender.Criteria.RequirementGroup.convert(): FindCriteriaResult.Criterion.RequirementGroup =
    FindCriteriaResult.Criterion.RequirementGroup(
        id = this.id,
        description = this.description,
        requirements = this.requirements
    )