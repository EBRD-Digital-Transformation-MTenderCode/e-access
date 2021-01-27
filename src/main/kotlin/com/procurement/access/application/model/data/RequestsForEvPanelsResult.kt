package com.procurement.access.application.model.data

import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.requirement.Requirement

data class RequestsForEvPanelsResult(
    val criteria: Criteria
) {
    data class Criteria(
        val id: String,
        val title: String,
        val source: CriteriaSource,
        val relatesTo: CriteriaRelatesTo,
        val description: String?,
        val classification: Classification,
        val requirementGroups: List<RequirementGroup>
    ) {
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
