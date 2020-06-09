package com.procurement.access.application.service.criteria

import com.procurement.access.application.model.criteria.CreatedCriteria
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.AwardCriteriaDetails
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.infrastructure.dto.cn.OpenCnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.SelectiveCnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.criteria.ConversionRequest
import com.procurement.access.infrastructure.dto.cn.criteria.CriterionRequest
import com.procurement.access.infrastructure.dto.cn.item.ItemReferenceRequest
import com.procurement.access.infrastructure.service.command.buildCriteria
import com.procurement.access.infrastructure.service.command.checkActualItemRelation
import com.procurement.access.infrastructure.service.command.checkAwardCriteriaDetailsAreRequired
import com.procurement.access.infrastructure.service.command.checkAwardCriteriaDetailsEnum
import com.procurement.access.infrastructure.service.command.checkAwardCriteriaEnum
import com.procurement.access.infrastructure.service.command.checkCastCoefficient
import com.procurement.access.infrastructure.service.command.checkCoefficient
import com.procurement.access.infrastructure.service.command.checkCoefficientDataType
import com.procurement.access.infrastructure.service.command.checkCoefficientRelatedOption
import com.procurement.access.infrastructure.service.command.checkCoefficientValueUniqueness
import com.procurement.access.infrastructure.service.command.checkConversionArrays
import com.procurement.access.infrastructure.service.command.checkConversionRelatesToEnum
import com.procurement.access.infrastructure.service.command.checkConversionRelation
import com.procurement.access.infrastructure.service.command.checkConversionWithoutCriteria
import com.procurement.access.infrastructure.service.command.checkCriteriaAndConversionAreRequired
import com.procurement.access.infrastructure.service.command.checkCriteriaWithAwardCriteria
import com.procurement.access.infrastructure.service.command.checkCriterionArrays
import com.procurement.access.infrastructure.service.command.checkDatatypeCompliance
import com.procurement.access.infrastructure.service.command.checkDateTime
import com.procurement.access.infrastructure.service.command.checkItemArrays
import com.procurement.access.infrastructure.service.command.checkMinMaxValue
import com.procurement.access.infrastructure.service.command.checkRequirements

interface CriteriaService {
    fun check(data: OpenCnOnPnRequest)

    fun check(data: SelectiveCnOnPnRequest)

    fun create(tender: OpenCnOnPnRequest.Tender): CreatedCriteria
}

class CriteriaServiceImpl : CriteriaService {

    override fun check(data: OpenCnOnPnRequest) {
        val tender = data.tender
        check(
            data.mainProcurementCategory,
            tender.awardCriteria,
            tender.awardCriteriaDetails,
            data.items,
            tender.criteria,
            tender.conversions
        )
    }

    override fun check(data: SelectiveCnOnPnRequest) {
        val tender = data.tender
        check(
            data.mainProcurementCategory,
            tender.awardCriteria,
            tender.awardCriteriaDetails,
            data.items,
            tender.criteria,
            tender.conversions
        )
    }

    private fun check(
        mainProcurementCategory: MainProcurementCategory?,
        awardCriteria: AwardCriteria?,
        awardCriteriaDetails: AwardCriteriaDetails?,
        items: List<ItemReferenceRequest>,
        criteria: List<CriterionRequest>?,
        conversions: List<ConversionRequest>?
    ) {

        checkConversionWithoutCriteria(criteria, conversions)

        // FReq-1.1.1.22
        checkAwardCriteriaDetailsAreRequired(awardCriteria, awardCriteriaDetails)

        // FReq-1.1.1.23
        checkCriteriaAndConversionAreRequired(awardCriteria, awardCriteriaDetails, criteria, conversions)

        // FReq-1.1.1.24
        checkCoefficientValueUniqueness(conversions)

        // FReq-1.1.1.27
        checkCriteriaWithAwardCriteria(awardCriteria, criteria, conversions)

        // FReq-1.1.1.3
        checkActualItemRelation(criteria, items)

        // FReq-1.1.1.4
        checkDatatypeCompliance(criteria)

        // FReq-1.1.1.5
        checkMinMaxValue(criteria)

        // FReq-1.1.1.6
        checkDateTime(criteria)

        // FReq-1.1.1.8
        checkRequirements(criteria)

        // FReq-1.1.1.9  & FReq-1.1.1.10
        checkConversionRelation(criteria, conversions)

        // FReq-1.1.1.11
        checkCoefficient(conversions)

        // FReq-1.1.1.12
        checkCoefficientDataType(criteria, conversions)

        // FReq-1.1.1.13
        checkCastCoefficient(mainProcurementCategory, criteria, conversions, items)

        // FReq-1.1.1.28
        checkCoefficientRelatedOption(criteria, conversions)

        // FReq-1.1.1.14
        checkConversionRelatesToEnum(conversions)

        // FReq-1.1.1.15
        checkAwardCriteriaEnum(awardCriteria)

        // FReq-1.1.1.15
        checkAwardCriteriaDetailsEnum(awardCriteriaDetails)

        // FReq-1.1.1.16
        checkItemArrays(items)
        checkCriterionArrays(criteria)
        checkConversionArrays(conversions)
    }

    override fun create(tender: OpenCnOnPnRequest.Tender): CreatedCriteria =
        buildCriteria(
            awardCriteria = tender.awardCriteria!!,
            awardCriteriaDetails = tender.awardCriteriaDetails,
            criteria = tender.criteria.orEmpty(),
            conversions = tender.conversions.orEmpty()
        )
}
