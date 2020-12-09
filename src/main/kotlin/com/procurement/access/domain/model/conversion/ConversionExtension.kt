package com.procurement.access.domain.model.conversion

import com.procurement.access.application.model.criteria.CoefficientId
import com.procurement.access.application.model.criteria.ConversionId
import com.procurement.access.application.model.criteria.RequirementId
import com.procurement.access.domain.model.enums.ConversionsRelatesTo
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.v1.model.request.ConversionRequest

fun buildConversion(
    conversion: ConversionRequest,
    relatedTemporalWithPermanentRequirementId: Map<String, RequirementId.Permanent>
): CNEntity.Tender.Conversion {

    val relatedItem = if (conversion.relatesTo == ConversionsRelatesTo.REQUIREMENT)
        relatedTemporalWithPermanentRequirementId[conversion.relatedItem]
            ?.toString()
            ?: throw ErrorException(
                ErrorType.INVALID_CONVERSION,
                message = "Conversion relates to requirement that does not exists. " +
                    "Conversion.id=${conversion.id}, Conversion.relatedItem=${conversion.relatedItem}"
            )
    else
        conversion.relatedItem

    return CNEntity.Tender.Conversion(
        id = ConversionId.Permanent.generate().toString(),
        description = conversion.description,
        relatesTo = conversion.relatesTo,
        relatedItem = relatedItem,
        rationale = conversion.rationale,
        coefficients = conversion.coefficients
            .map { coefficient ->
                CNEntity.Tender.Conversion.Coefficient(
                    id = CoefficientId.Permanent.generate().toString(),
                    value = coefficient.value,
                    coefficient = coefficient.coefficient,
                    relatedOption = coefficient.relatedOption
                )
            }
    )
}
