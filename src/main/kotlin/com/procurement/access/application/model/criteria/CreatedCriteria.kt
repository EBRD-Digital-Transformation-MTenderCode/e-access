package com.procurement.access.application.model.criteria

import com.procurement.access.domain.model.coefficient.CoefficientRate
import com.procurement.access.domain.model.coefficient.CoefficientValue
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.AwardCriteriaDetails
import com.procurement.access.domain.model.enums.ConversionsRelatesTo
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.option.RelatedOption
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.entity.CNEntity

data class CreatedCriteria(
    val awardCriteria: AwardCriteria,
    val awardCriteriaDetails: AwardCriteriaDetails?,
    val criteria: List<Criteria>,
    val conversions: List<Conversion>
) {
    data class Criteria(
        val id: CriteriaId.Permanent,
        val title: String,
        val source: CriteriaSource?,
        val description: String?,
        val requirementGroups: List<RequirementGroup>,
        val relatesTo: CriteriaRelatesToEnum?,
        val relatedItem: String?
    ) {
        data class RequirementGroup(
            val id: RequirementGroupId.Permanent,
            val description: String?,
            val requirements: List<Requirement>
        )
    }

    data class Conversion(
        val id: ConversionId.Permanent,
        val relatesTo: ConversionsRelatesTo,
        val relatedItem: String,
        val rationale: String,
        val description: String?,
        val coefficients: List<Coefficient>
    ) {
        data class Coefficient(
            val id: CoefficientId.Permanent,
            val relatedOption: RelatedOption?,
            val value: CoefficientValue,
            val coefficient: CoefficientRate
        )
    }
}

fun CreatedCriteria.Criteria.toEntity(): CNEntity.Tender.Criteria =
    CNEntity.Tender.Criteria(
        id = this.id.toString(),
        title = this.title,
        description = this.description,
        requirementGroups = this.requirementGroups
            .map { it.toEntity() },
        relatesTo = this.relatesTo,
        relatedItem = this.relatedItem
    )

fun CreatedCriteria.Criteria.RequirementGroup.toEntity(): CNEntity.Tender.Criteria.RequirementGroup =
    CNEntity.Tender.Criteria.RequirementGroup(
        id = this.id.toString(),
        description = this.description,
        requirements = this.requirements
    )
