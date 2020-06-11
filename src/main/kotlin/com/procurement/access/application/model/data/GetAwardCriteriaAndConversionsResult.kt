package com.procurement.access.application.model.data

import com.procurement.access.domain.model.coefficient.CoefficientRate
import com.procurement.access.domain.model.coefficient.CoefficientValue
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.AwardCriteriaDetails
import com.procurement.access.domain.model.enums.ConversionsRelatesTo

data class GetAwardCriteriaAndConversionsResult(
    val awardCriteria: AwardCriteria,
    val awardCriteriaDetails: AwardCriteriaDetails,
    val conversions: List<Conversion>?
) {
    data class Conversion(
        val id: String,
        val relatesTo: ConversionsRelatesTo,
        val relatedItem: String,
        val rationale: String,
        val description: String?,

        val coefficients: List<Coefficient>
    ) {
        data class Coefficient(
            val id: String,
            val value: CoefficientValue,
            val coefficient: CoefficientRate
        )
    }
}
