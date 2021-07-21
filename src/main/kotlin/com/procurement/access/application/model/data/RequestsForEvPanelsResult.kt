package com.procurement.access.application.model.data

import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.infrastructure.entity.CNEntity

data class RequestsForEvPanelsResult(
    val criteria: List<Criterion>
) {
    data class Criterion(
        val id: String,
        val title: String,
        val source: CriteriaSource,
        val relatesTo: CriteriaRelatesTo,
        val description: String?,
        val classification: Classification,
        val requirementGroups: List<RequirementGroup>
    ) {
        companion object;

        data class RequirementGroup(
            val id: String,
            val requirements: List<Requirement>
        )

        data class Classification(
            val scheme: String,
            val id: String
        )
    }
}

fun RequestsForEvPanelsResult.Criterion.Companion.fromDomain(criterion: CNEntity.Tender.Criteria) =
    RequestsForEvPanelsResult.Criterion(
        id = criterion.id,
        title = criterion.title,
        description = criterion.description,
        source = criterion.source!!,
        relatesTo = criterion.relatesTo!!,
        classification = criterion.classification
            .let { classification ->
                RequestsForEvPanelsResult.Criterion.Classification(
                    id = classification.id,
                    scheme = classification.scheme
                )
            },
        requirementGroups = criterion.requirementGroups
            .map { requirementGroup ->
                RequestsForEvPanelsResult.Criterion.RequirementGroup(
                    id = requirementGroup.id,
                    requirements = requirementGroup.requirements
                        .map { requirement ->
                            Requirement(
                                id = requirement.id,
                                title = requirement.title,
                                dataType = requirement.dataType,
                                value = requirement.value,
                                period = requirement.period,
                                description = requirement.description,
                                eligibleEvidences = requirement.eligibleEvidences?.toList(),
                                status = requirement.status,
                                datePublished = requirement.datePublished
                            )
                        }
                )
            }

    )
