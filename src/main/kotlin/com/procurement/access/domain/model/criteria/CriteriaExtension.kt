package com.procurement.access.domain.model.criteria

import com.procurement.access.application.model.criteria.CriteriaId
import com.procurement.access.application.model.criteria.RequirementGroupId
import com.procurement.access.application.model.criteria.RequirementId
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.infrastructure.dto.cn.criteria.CriterionRequest
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.entity.CNEntity

fun generatePermanentRequirementIds(criteria: List<CriterionRequest>?): Map<String, RequirementId.Permanent> =
    criteria
        ?.asSequence()
        ?.flatMap { criterion ->
            criterion.requirementGroups.asSequence()
        }
        ?.flatMap { group ->
            group.requirements.asSequence()
        }
        ?.map { requirement ->
            requirement.id to RequirementId.Permanent.generate() as RequirementId.Permanent
        }
        ?.toMap()
        ?: emptyMap()

fun buildCriterion(
    criterion: CriterionRequest,
    relatedTemporalWithPermanentRequirementId: Map<String, RequirementId.Permanent>
): CNEntity.Tender.Criteria {
    val source = criterion.defineSource()
    return CNEntity.Tender.Criteria(
        id = CriteriaId.Permanent.generate().toString(),
        title = criterion.title,
        description = criterion.description,
        relatesTo = criterion.relatesTo,
        relatedItem = criterion.relatedItem,
        source = source,
        requirementGroups = criterion.requirementGroups
            .map { requirementGroup ->
                CNEntity.Tender.Criteria.RequirementGroup(
                    id = RequirementGroupId.Permanent.generate().toString(),
                    description = requirementGroup.description,
                    requirements = requirementGroup.requirements
                        .map { requirement ->
                            Requirement(
                                id = relatedTemporalWithPermanentRequirementId.getValue(requirement.id).toString(),
                                title = requirement.title,
                                description = requirement.description,
                                period = requirement.period
                                    ?.let { period ->
                                        Requirement.Period(
                                            startDate = period.startDate,
                                            endDate = period.endDate
                                        )
                                    },
                                dataType = requirement.dataType,
                                value = requirement.value
                            )
                        }
                )
            }
    )
}

private fun CriterionRequest.defineSource(): CriteriaSource? = when (relatesTo) {
    CriteriaRelatesToEnum.TENDERER -> null
    CriteriaRelatesToEnum.LOT -> CriteriaSource.TENDERER
    CriteriaRelatesToEnum.ITEM -> CriteriaSource.TENDERER
    null -> CriteriaSource.TENDERER
}

fun CNEntity.Tender.Criteria.replaceTemporalItemId(
    relatedTemporalWithPermanentLotId: Map<String, String>,
    relatedTemporalWithPermanentItemId: Map<String, String>
): CNEntity.Tender.Criteria {
    val relatesTo = this.relatesTo
    val relatedItem = this.relatedItem
    return this.copy(
        relatedItem = when (relatesTo) {
            CriteriaRelatesToEnum.LOT -> relatedTemporalWithPermanentLotId.getValue(relatedItem!!)
            CriteriaRelatesToEnum.ITEM -> relatedTemporalWithPermanentItemId.getValue(relatedItem!!)
            CriteriaRelatesToEnum.TENDERER -> relatedItem
            null -> relatedItem
        }
    )
}
