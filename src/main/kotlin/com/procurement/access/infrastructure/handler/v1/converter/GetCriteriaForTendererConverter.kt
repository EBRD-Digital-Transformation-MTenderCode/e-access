package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.data.GetCriteriaForTendererResult
import com.procurement.access.infrastructure.handler.v1.model.response.GetCriteriaForTendererResponse

fun GetCriteriaForTendererResult.convert(): GetCriteriaForTendererResponse =
    GetCriteriaForTendererResponse(
        criteria = criteria.map { it.convert() }
    )

fun GetCriteriaForTendererResult.Criterion.convert(): GetCriteriaForTendererResponse.Criterion =
    GetCriteriaForTendererResponse.Criterion(
        id = this.id,
        title = this.title,
        description = this.description,
        source = this.source,
        relatesTo = this.relatesTo,
        relatedItem = this.relatedItem,
        classification = this.classification?.convert(),
        requirementGroups = this.requirementGroups.map { it.convert() }
    )

fun GetCriteriaForTendererResult.Criterion.Classification.convert(): GetCriteriaForTendererResponse.Criterion.Classification =
    GetCriteriaForTendererResponse.Criterion.Classification(
        id = this.id,
        scheme = this.scheme
    )

fun GetCriteriaForTendererResult.Criterion.RequirementGroup.convert(): GetCriteriaForTendererResponse.Criterion.RequirementGroup =
    GetCriteriaForTendererResponse.Criterion.RequirementGroup(
        id = this.id,
        description = this.description,
        requirements = this.requirements
    )
