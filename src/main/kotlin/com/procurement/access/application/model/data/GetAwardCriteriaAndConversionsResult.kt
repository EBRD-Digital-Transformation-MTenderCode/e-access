package com.procurement.access.application.model.data

import com.procurement.access.domain.model.coefficient.CoefficientRate
import com.procurement.access.domain.model.coefficient.CoefficientValue
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.AwardCriteriaDetails
import com.procurement.access.domain.model.enums.ConversionsRelatesTo
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.RfqEntity

data class GetAwardCriteriaAndConversionsResult(
    val awardCriteria: AwardCriteria,
    val awardCriteriaDetails: AwardCriteriaDetails?,
    val conversions: List<Conversion>?
) { companion object {}

    data class Conversion(
        val id: String,
        val relatesTo: ConversionsRelatesTo,
        val relatedItem: String,
        val rationale: String,
        val description: String?,
        val coefficients: List<Coefficient>
    ) { companion object {}

        data class Coefficient(
            val id: String,
            val value: CoefficientValue,
            val coefficient: CoefficientRate
        ) { companion object {} }
    }
}

fun GetAwardCriteriaAndConversionsResult.Companion.fromDomain(cn: CNEntity): GetAwardCriteriaAndConversionsResult =
    GetAwardCriteriaAndConversionsResult(
        awardCriteria = cn.tender.awardCriteria!!,
        awardCriteriaDetails = cn.tender.awardCriteriaDetails,
        conversions = cn.tender.conversions
            ?.map { GetAwardCriteriaAndConversionsResult.Conversion.fromDomain(it) }
    )

fun GetAwardCriteriaAndConversionsResult.Conversion.Companion.fromDomain(conversion: CNEntity.Tender.Conversion) =
    GetAwardCriteriaAndConversionsResult.Conversion(
        id = conversion.id,
        relatesTo = conversion.relatesTo,
        relatedItem = conversion.relatedItem,
        description = conversion.description,
        rationale = conversion.rationale,
        coefficients = conversion.coefficients
            .map { GetAwardCriteriaAndConversionsResult.Conversion.Coefficient.fromDomain(it) }
    )

fun GetAwardCriteriaAndConversionsResult.Conversion.Coefficient.Companion.fromDomain(coefficient: CNEntity.Tender.Conversion.Coefficient) =
    GetAwardCriteriaAndConversionsResult.Conversion.Coefficient(
        id = coefficient.id,
        value = coefficient.value,
        coefficient = coefficient.coefficient
    )

fun GetAwardCriteriaAndConversionsResult.Companion.fromDomain(rfq: RfqEntity): GetAwardCriteriaAndConversionsResult =
    GetAwardCriteriaAndConversionsResult(
        awardCriteria = rfq.tender.awardCriteria,
        awardCriteriaDetails = rfq.tender.awardCriteriaDetails,
        conversions = emptyList()
    )
