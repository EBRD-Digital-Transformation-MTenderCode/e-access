package com.procurement.access.application.model.data

import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.FEEntity

data class GetCriteriaForTendererResult(val criteria: List<Criterion>) {
    data class Criterion(
        val id: String,
        val title: String,
        val classification: Classification?,
        val source: CriteriaSource?,
        val description: String?,
        val requirementGroups: List<RequirementGroup>,
        val relatesTo: CriteriaRelatesTo?,
        val relatedItem: String?
    ) {

        data class Classification(
            val id: String,
            val scheme: String
        )

        data class RequirementGroup(
            val id: String,
            val description: String?,
            val requirements: List<Requirement>
        )
    }

    companion object {
        fun fromDomain(criterion: CNEntity.Tender.Criteria): Criterion =
            Criterion(
                id = criterion.id,
                title = criterion.title,
                description = criterion.description,
                relatedItem = criterion.relatedItem,
                relatesTo = criterion.relatesTo,
                source = criterion.source,
                classification = criterion.classification.let { fromDomain(it) },
                requirementGroups = criterion.requirementGroups.map { fromDomain(it) }
            )

        private fun fromDomain(classification: CNEntity.Tender.Criteria.Classification): Criterion.Classification =
            Criterion.Classification(
                id = classification.id,
                scheme = classification.scheme
            )

        private fun fromDomain(classification: CNEntity.Tender.Criteria.RequirementGroup): Criterion.RequirementGroup =
            Criterion.RequirementGroup(
                id = classification.id,
                description = classification.description,
                requirements = classification.requirements
            )

        fun fromDomain(criterion: FEEntity.Tender.Criteria): Criterion =
            Criterion(
                id = criterion.id,
                title = criterion.title,
                description = criterion.description,
                relatedItem = null,
                relatesTo = criterion.relatesTo,
                source = criterion.source,
                classification = criterion.classification?.let { fromDomain(it) },
                requirementGroups = criterion.requirementGroups.map { fromDomain(it) }
            )

        private fun fromDomain(classification: FEEntity.Tender.Criteria.Classification): Criterion.Classification =
            Criterion.Classification(
                id = classification.id,
                scheme = classification.scheme
            )

        private fun fromDomain(classification: FEEntity.Tender.Criteria.RequirementGroup): Criterion.RequirementGroup =
            Criterion.RequirementGroup(
                id = classification.id,
                description = classification.description,
                requirements = classification.requirements
            )
    }
}
