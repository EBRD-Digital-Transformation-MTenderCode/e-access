package com.procurement.access.application.model.data

import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.requirement.Requirement

data class RequestsForEvPanelsResult(
    val criteria: Criteria
) {
    data class Criteria(
        val id: String,
        val title: String,
        val source: CriteriaSource,
        val relatesTo: CriteriaRelatesToEnum,
        val description: String?,
        val requirementGroups: List<RequirementGroup>
    ) {
        data class RequirementGroup(
            val id: String,
            val requirements: List<Requirement>
        )
    }
}
