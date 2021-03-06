package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.data.GetAwardCriteriaAndConversionsResult
import com.procurement.access.infrastructure.handler.v1.model.response.GetCriteriaResponse

fun GetAwardCriteriaAndConversionsResult.convert() =
    GetCriteriaResponse(
        awardCriteria = this.awardCriteria,
        awardCriteriaDetails = this.awardCriteriaDetails,
        conversions = conversions
            ?.map { conversion ->
                GetCriteriaResponse.Conversion(
                    id = conversion.id,
                    relatesTo = conversion.relatesTo,
                    relatedItem = conversion.relatedItem,
                    description = conversion.description,
                    rationale = conversion.rationale,
                    coefficients = conversion.coefficients
                        .map { coefficient ->
                            GetCriteriaResponse.Conversion.Coefficient(
                                id = coefficient.id,
                                value = coefficient.value,
                                coefficient = coefficient.coefficient
                            )
                        }
                )
            }
    )
