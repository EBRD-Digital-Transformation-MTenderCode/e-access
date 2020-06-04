package com.procurement.access.infrastructure.service.command

import com.procurement.access.application.model.criteria.CoefficientId
import com.procurement.access.application.model.criteria.ConversionId
import com.procurement.access.application.model.criteria.CreatedCriteria
import com.procurement.access.application.model.criteria.CriteriaId
import com.procurement.access.application.model.criteria.RequirementGroupId
import com.procurement.access.application.model.criteria.RequirementId
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.AwardCriteriaDetails
import com.procurement.access.domain.model.enums.ConversionsRelatesTo
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.CnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement

fun buildCriteria(
    awardCriteria: AwardCriteria,
    awardCriteriaDetails: AwardCriteriaDetails?,
    criteria: List<CnOnPnRequest.Tender.Criteria>,
    conversions: List<CnOnPnRequest.Tender.Conversion>
): CreatedCriteria {
    fun replaceConversionRelation(
        conversion: CnOnPnRequest.Tender.Conversion,
        relations: Map<String, String>
    ): String {
        if (conversion.relatesTo == ConversionsRelatesTo.REQUIREMENT) return relations.get(conversion.relatedItem)
            ?: throw ErrorException(
                ErrorType.INVALID_CONVERSION,
                message = "Conversion relates to requirement that does not exists. " +
                    "Conversion.id=${conversion.id}, Conversion.relatedItem=${conversion.relatedItem}"
            )
        return conversion.relatedItem
    }

    val requirementTempToPermanentIdRelation = mutableMapOf<String, String>()

    return CreatedCriteria(
        criteria = criteria.map { criterion ->
            CreatedCriteria.Criteria(
                id = CriteriaId.Permanent.generate() as CriteriaId.Permanent,
                title = criterion.title,
                description = criterion.description,
                relatesTo = criterion.relatesTo,
                relatedItem = criterion.relatedItem,
                source = defineSource(criterion),
                requirementGroups = criterion.requirementGroups
                    .map { rg ->
                        CreatedCriteria.Criteria.RequirementGroup(
                            id = RequirementGroupId.Permanent.generate() as RequirementGroupId.Permanent,
                            description = rg.description,
                            requirements = rg.requirements
                                .map { requirement ->
                                    val permanentId = RequirementId.Permanent.generate().toString()
                                    requirementTempToPermanentIdRelation.put(requirement.id, permanentId)
                                    Requirement(
                                        id = permanentId,
                                        title = requirement.title,
                                        description = requirement.description,
                                        period = requirement.period,
                                        dataType = requirement.dataType,
                                        value = requirement.value
                                    )
                                }
                        )
                    }
            )
        },
        conversions = conversions.map { conversion ->
            CreatedCriteria.Conversion(
                id = ConversionId.Permanent.generate() as ConversionId.Permanent,
                description = conversion.description,
                relatesTo = conversion.relatesTo,
                relatedItem = replaceConversionRelation(conversion, requirementTempToPermanentIdRelation),
                rationale = conversion.rationale,
                coefficients = conversion.coefficients
                    .map { coefficient ->
                        CreatedCriteria.Conversion.Coefficient(
                            id = CoefficientId.Permanent.generate() as CoefficientId.Permanent,
                            value = coefficient.value,
                            coefficient = coefficient.coefficient,
                            relatedOption = coefficient.relatedOption
                        )
                    }

            )
        },
        awardCriteria = awardCriteria,
        awardCriteriaDetails = setAwardCriteriaDetails(awardCriteria) ?: awardCriteriaDetails
    )
}


private fun defineSource(criteria: CnOnPnRequest.Tender.Criteria): CriteriaSource? =
    if (criteria.relatesTo == null || criteria.relatesTo != CriteriaRelatesToEnum.TENDERER)
        CriteriaSource.TENDERER
    else
        null

private fun setAwardCriteriaDetails(awardCriteria: AwardCriteria): AwardCriteriaDetails? =
    when (awardCriteria) {
        AwardCriteria.PRICE_ONLY -> AwardCriteriaDetails.AUTOMATED
        AwardCriteria.COST_ONLY,
        AwardCriteria.QUALITY_ONLY,
        AwardCriteria.RATED_CRITERIA -> null
    }
