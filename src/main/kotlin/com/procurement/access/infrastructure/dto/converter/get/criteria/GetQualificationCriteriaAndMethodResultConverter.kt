package com.procurement.access.infrastructure.dto.converter.get.criteria

import com.procurement.access.domain.model.enums.QualificationSystemMethod
import com.procurement.access.domain.model.enums.ReductionCriteria
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.v2.model.response.GetQualificationCriteriaAndMethodResult

fun convert(
    conversions: List<CNEntity.Tender.Conversion>,
    qualificationSystemMethods: List<QualificationSystemMethod>,
    reductionCriteria: ReductionCriteria
): GetQualificationCriteriaAndMethodResult =
    GetQualificationCriteriaAndMethodResult(
        conversions                = conversions.map { it.convert() },
        reductionCriteria          = reductionCriteria,
        qualificationSystemMethods = qualificationSystemMethods
    )

private fun CNEntity.Tender.Conversion.convert(): GetQualificationCriteriaAndMethodResult.Conversion =
    GetQualificationCriteriaAndMethodResult.Conversion(
        id           = this.id,
        relatesTo    = this.relatesTo,
        relatedItem  = this.relatedItem,
        rationale    = this.rationale,
        description  = this.description,
        coefficients = this.coefficients.map { it.convert() }
    )

private fun CNEntity.Tender.Conversion.Coefficient.convert(): GetQualificationCriteriaAndMethodResult.Conversion.Coefficient =
    GetQualificationCriteriaAndMethodResult.Conversion.Coefficient(
        id          = this.id,
        value       = this.value,
        coefficient = this.coefficient
    )