package com.procurement.access.domain.model.criteria

import com.procurement.access.application.model.criteria.CriteriaId
import com.procurement.access.application.model.criteria.RequirementGroupId
import com.procurement.access.application.model.criteria.RequirementId
import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.RequirementStatus
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.v1.model.request.criterion.CriterionRequest
import java.time.LocalDateTime

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
    datePublished: LocalDateTime,
    criterion: CriterionRequest,
    relatedTemporalWithPermanentRequirementId: Map<String, RequirementId.Permanent>
): CNEntity.Tender.Criteria {
    return CNEntity.Tender.Criteria(
        id = CriteriaId.Permanent.generate().toString(),
        title = criterion.title,
        description = criterion.description,
        classification = criterion.classification
            .let { classification ->
                CNEntity.Tender.Criteria.Classification(
                    id = classification.id,
                    scheme = classification.scheme
                )
            },
        relatesTo = criterion.relatesTo,
        relatedItem = criterion.relatedItem,
        source = CriteriaSource.TENDERER,
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
                                value = requirement.value,
                                eligibleEvidences = requirement.eligibleEvidences?.toList(),
                                status = RequirementStatus.ACTIVE,
                                datePublished = datePublished
                            )
                        }
                )
            }
    )
}

fun CNEntity.Tender.Criteria.replaceTemporalItemId(
    relatedTemporalWithPermanentLotId: Map<String, String>,
    relatedTemporalWithPermanentItemId: Map<String, String>
): CNEntity.Tender.Criteria {
    val relatesTo = this.relatesTo
    val relatedItem = this.relatedItem
    return this.copy(
        relatedItem = when (relatesTo) {
            CriteriaRelatesTo.AWARD -> relatedTemporalWithPermanentItemId.getValue(relatedItem!!)
            CriteriaRelatesTo.ITEM -> relatedTemporalWithPermanentItemId.getValue(relatedItem!!)
            CriteriaRelatesTo.LOT -> relatedTemporalWithPermanentLotId.getValue(relatedItem!!)

            CriteriaRelatesTo.QUALIFICATION,
            CriteriaRelatesTo.TENDER,
            CriteriaRelatesTo.TENDERER,
            null -> relatedItem
        }
    )
}
