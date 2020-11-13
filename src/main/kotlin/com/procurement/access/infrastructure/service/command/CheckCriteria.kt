package com.procurement.access.infrastructure.service.command

import com.procurement.access.domain.model.coefficient.CoefficientValue
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.AwardCriteriaDetails
import com.procurement.access.domain.model.enums.ConversionsRelatesTo
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.util.extension.toSetBy
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.criteria.ConversionRequest
import com.procurement.access.infrastructure.dto.cn.criteria.CriterionRequest
import com.procurement.access.infrastructure.dto.cn.criteria.ExpectedValue
import com.procurement.access.infrastructure.dto.cn.criteria.MaxValue
import com.procurement.access.infrastructure.dto.cn.criteria.MinValue
import com.procurement.access.infrastructure.dto.cn.criteria.NoneValue
import com.procurement.access.infrastructure.dto.cn.criteria.RangeValue
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.dto.cn.criteria.RequirementValue
import com.procurement.access.infrastructure.dto.cn.item.ItemReferenceRequest
import java.math.BigDecimal

fun checkCriteriaAndConversion(
    mainProcurementCategory: MainProcurementCategory?,
    awardCriteria: AwardCriteria,
    awardCriteriaDetails: AwardCriteriaDetails?,
    items: List<ItemReferenceRequest>,
    criteria: List<CriterionRequest>?,
    conversions: List<ConversionRequest>?
) {

    // FReq-1.1.1.16
    checkItemArrays(items)
    checkCriterionArrays(criteria)
    checkConversionArrays(conversions)

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
}

fun checkConversionWithoutCriteria(criteria: List<CriterionRequest>?, conversions: List<ConversionRequest>?) {
    if (criteria == null && conversions != null)
        throw ErrorException(ErrorType.INVALID_CONVERSION, message = "Conversions cannot exists without criteria")
}

fun checkActualItemRelation(criteria: List<CriterionRequest>?, items: List<ItemReferenceRequest>) {
    fun Map<String, ItemReferenceRequest>.containsElement(itemId: String) {
        if (!this.containsKey(itemId)) throw ErrorException(
            ErrorType.INVALID_CRITERIA,
            message = "Criteria relates to item that does not exists. Item id=${itemId}, Available items: ${this.keys}"
        )
    }

    fun Map<String, List<ItemReferenceRequest>>.relatesWithLot(lotId: String) {
        if (!this.containsKey(lotId)) throw ErrorException(
            ErrorType.INVALID_CRITERIA,
            message = "Criteria relates to lot that does not exists. Item id=${lotId}, Available lots: ${this.keys}"
        )
    }

    fun CriterionRequest.validate(
        itemsById: Map<String, ItemReferenceRequest>,
        itemsByRelatedLot: Map<String, List<ItemReferenceRequest>>
    ) {
        when (this.relatesTo) {
            CriteriaRelatesToEnum.ITEM -> itemsById.containsElement(this.relatedItem!!)
            CriteriaRelatesToEnum.LOT -> itemsByRelatedLot.relatesWithLot(this.relatedItem!!)
            CriteriaRelatesToEnum.TENDERER -> Unit
        }
    }

    fun CriterionRequest.validateRelation() {
        if (this.relatesTo == null && this.relatedItem != null) throw ErrorException(
            error = ErrorType.INVALID_CRITERIA,
            message = "Criteria has reletedItem attribute but missing relatedTo"
        )

        if (this.relatesTo != null) {
            when (this.relatesTo) {
                CriteriaRelatesToEnum.TENDERER -> if (this.relatedItem != null) throw ErrorException(
                    error = ErrorType.INVALID_CRITERIA,
                    message = "For parameter relatedTo = 'tenderer', parameter relatedItem cannot be passed"
                )
                CriteriaRelatesToEnum.ITEM,
                CriteriaRelatesToEnum.LOT -> if (this.relatedItem == null) throw ErrorException(
                    error = ErrorType.INVALID_CRITERIA,
                    message = "For parameter relatedTo = 'lot' or 'item', parameter relatedItem must be specified"
                )
            }
        }
    }

    if (criteria == null) return

    val itemsById = items.associateBy { it.id }
    val itemsByRelatedLot = items.groupBy { it.relatedLot }

    criteria.forEach { _criteria ->
        _criteria.validateRelation()
        _criteria.validate(itemsById, itemsByRelatedLot)
    }
}

fun checkDatatypeCompliance(criteria: List<CriterionRequest>?) {
    fun mismatchDatatypeException(rv: RequirementValue?, rDatatype: RequirementDataType): Nothing =
        throw ErrorException(
            ErrorType.INVALID_CRITERIA,
            message = "Requirement.dataType mismatch with datatype in expectedValue || minValue || maxValue. " +
                "${rv} != ${rDatatype}"
        )

    fun Requirement.hasRequirementValue(): Boolean = this.value != NoneValue
    fun Requirement.validate() {
        if (!this.hasRequirementValue()) return

        when (this.value) {
            is ExpectedValue.AsBoolean -> if (this.dataType != RequirementDataType.BOOLEAN)
                mismatchDatatypeException(this.value, this.dataType)

            is ExpectedValue.AsString -> if (this.dataType != RequirementDataType.STRING)
                mismatchDatatypeException(this.value, this.dataType)

            is ExpectedValue.AsInteger,
            is MinValue.AsInteger,
            is MaxValue.AsInteger,
            is RangeValue.AsInteger -> if (this.dataType != RequirementDataType.INTEGER)
                mismatchDatatypeException(this.value, this.dataType)

            is ExpectedValue.AsNumber,
            is MinValue.AsNumber,
            is MaxValue.AsNumber,
            is RangeValue.AsNumber -> if (this.dataType != RequirementDataType.NUMBER)
                mismatchDatatypeException(this.value, this.dataType)
        }
    }

    criteria?.asSequence()
        ?.flatMap { it.requirementGroups.asSequence() }
        ?.flatMap { it.requirements.asSequence() }
        ?.forEach { it.validate() }
}

fun checkMinMaxValue(criteria: List<CriterionRequest>?) {
    fun rangeException(): Nothing = throw ErrorException(
        ErrorType.INVALID_REQUIREMENT_VALUE,
        message = "minValue greater than or equals to maxValue"
    )

    fun <T : Number> validateRange(minValue: T, maxValue: T) {
        when (minValue) {
            is Long -> if (minValue >= maxValue.toLong()) rangeException()
            is BigDecimal -> if (minValue >= java.math.BigDecimal(maxValue.toString())) rangeException()
        }
    }

    fun RangeValue.validate() {
        when (this) {
            is RangeValue.AsNumber -> validateRange(minValue = this.minValue, maxValue = this.maxValue)
            is RangeValue.AsInteger -> validateRange(minValue = this.minValue, maxValue = this.maxValue)
        }
    }

    criteria?.asSequence()
        ?.flatMap { it.requirementGroups.asSequence() }
        ?.flatMap { it.requirements.asSequence() }
        ?.forEach { if (it.value is RangeValue) it.value.validate() }
}

fun checkDateTime(criteria: List<CriterionRequest>?) {
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    fun Requirement.Period.validate() {
        if (this.startDate.year > currentYear || this.endDate.year > currentYear)
            throw ErrorException(
                ErrorType.INVALID_PERIOD_VALUE,
                message = "start/endDate year cannot be greater than current year. " +
                    "StartDate=${this.startDate}, " +
                    "EndDate=${this.endDate}, " +
                    "Current year = ${currentYear}"
            )
        if (this.startDate > this.endDate)
            throw ErrorException(
                ErrorType.INVALID_PERIOD_VALUE,
                message = "startDate cannot be greater than endDate. StartDate=${this.startDate}, EndDate=${this.endDate}"
            )
    }

    criteria?.asSequence()
        ?.flatMap { it.requirementGroups.asSequence() }
        ?.flatMap { it.requirements.asSequence() }
        ?.forEach { it.period?.validate() }
}

fun checkRequirements(criteria: List<CriterionRequest>?) {

    if (criteria == null) return
    val criteriaById = criteria.groupBy { it.id }
    val requirementGroupById = criteria.flatMap { it.requirementGroups }
        .groupBy { it.id }
    val requirementById = criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .groupBy { it.id }

    fun CriterionRequest.validateId() {
        val criteriaId = criteriaById.get(this.id)
        if (criteriaId != null && criteriaId.size > 1)
            throw ErrorException(
                ErrorType.INVALID_CRITERIA,
                message = "criteria.id is not unique. Current id = ${this.id}, All ids: ${criteriaById}"
            )
    }

    fun CriterionRequest.RequirementGroup.validateId() {
        val requirementGroup = requirementGroupById.get(this.id)
        if (requirementGroup != null && requirementGroup.size > 1)
            throw ErrorException(
                ErrorType.INVALID_CRITERIA,
                message = "requirementGroup.id is not unique. Current id = ${this.id}. All ids: ${requirementGroupById}"
            )
    }

    fun Requirement.validateId() {
        val requirement = requirementById.get(this.id)
        if (requirement != null && requirement.size > 1)
            throw ErrorException(
                ErrorType.INVALID_CRITERIA,
                message = "requirement.id is not unique. Current id = ${this.id}. All ids: ${requirementById}"
            )
    }

    fun List<CriterionRequest.RequirementGroup>.validateRequirementGroupCount() {
        if (this.isEmpty())
            throw ErrorException(
                ErrorType.INVALID_CRITERIA,
                message = "Must be at least one requirementGroup have to be added"
            )
    }

    fun List<Requirement>.validateRequirementsCount() {
        if (this.isEmpty())
            throw ErrorException(
                ErrorType.INVALID_CRITERIA,
                message = "Must be at least one requirements have to be added"
            )
    }


    criteria.forEach {
        it.validateId()
        it.requirementGroups.validateRequirementGroupCount()

        it.requirementGroups.forEach { rg ->
            rg.validateId()
            rg.requirements.validateRequirementsCount()
            rg.requirements.forEach { requirement ->
                requirement.validateId()
            }
        }
    }
}

fun checkConversionRelation(criteria: List<CriterionRequest>?, conversions: List<ConversionRequest>?) {
    if (criteria == null || conversions == null) return

    val requirements = criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .groupBy { it.id }

    val relation: MutableMap<String, List<Requirement>> = mutableMapOf()
    relation.putAll(requirements)

    val conversionsRelatesToRequirement = conversions.filter { it.relatesTo == ConversionsRelatesTo.REQUIREMENT }

    conversionsRelatesToRequirement.map {
        if (!requirements.containsKey(it.relatedItem)) throw ErrorException(
            ErrorType.INVALID_CONVERSION,
            message = "Conversion relates to requirement that does not exists"
        )

        if (!relation.containsKey(it.relatedItem)) throw ErrorException(
            ErrorType.INVALID_CONVERSION,
            message = "Conversion relates to requirement that already in relation with another conversion"
        )
        relation.remove(it.relatedItem)
    }
}

fun checkCoefficient(conversions: List<ConversionRequest>?) {
    val COEFFICIENT_MIN = 0.01.toBigDecimal()
    val COEFFICIENT_MAX = 1.toBigDecimal()

    fun ConversionRequest.Coefficient.validateCoefficientRate() {
        if (this.coefficient.rate < COEFFICIENT_MIN || this.coefficient.rate > COEFFICIENT_MAX)
            throw ErrorException(
                ErrorType.INVALID_CONVERSION,
                message = "Conversion coefficient rate (${this.coefficient}) does not satisfy the conditions: " +
                    "coefficient in [${COEFFICIENT_MIN}, ${COEFFICIENT_MAX}]"
            )
    }

    conversions
        ?.asSequence()
        ?.flatMap { it.coefficients.asSequence() }
        ?.forEach { it.validateCoefficientRate() }
}

fun checkCoefficientValueUniqueness(conversions: List<ConversionRequest>?) {
    fun uniquenessException(coefficients: List<ConversionRequest.Coefficient>): Nothing = throw ErrorException(
        ErrorType.INVALID_CONVERSION,
        message = "Conversion coefficients value contains not unique element: " +
            "${coefficients.map { it.value }
                .map { if (it is CoefficientValue.AsNumber) it.value.stripTrailingZeros() else it }
                .groupBy { it }
                .filter { it.value.size > 1 }.keys}"
    )

    fun List<ConversionRequest.Coefficient>.validateCoefficientValues() {
            when (this[0].value) {
                is CoefficientValue.AsBoolean,
                is CoefficientValue.AsInteger,
                is CoefficientValue.AsNumber -> {
                    val values = this.map {
                        if (it.value is CoefficientValue.AsNumber) it.value.value.stripTrailingZeros() else it.value
                    }
                    if (values.toSet().size != values.size) uniquenessException(this)
                }
                is CoefficientValue.AsString -> Unit
            }

    }

    conversions?.forEach { conversion ->
        conversion.coefficients.validateCoefficientValues()
    }
}

fun checkCriteriaWithAwardCriteria(
    awardCriteria: AwardCriteria,
    criteria: List<CriterionRequest>?,
    conversions: List<ConversionRequest>?
) {
    if (criteria == null) return

    when (awardCriteria) {
        AwardCriteria.PRICE_ONLY -> {
            if (conversions != null) throw ErrorException(
                error = ErrorType.INVALID_CONVERSION,
                message = "For awardCriteria='priceOnly' conversion cannot be passed"
            )

            val nonTendererCriteria = criteria.filter { it.relatesTo != CriteriaRelatesToEnum.TENDERER }
            if (nonTendererCriteria.isNotEmpty()) throw ErrorException(
                error = ErrorType.INVALID_CRITERIA,
                message = "For awardCriteria='priceOnly' can be passed only criteria that relates to tenderer. " +
                    "Non tenderer criteria ids: ${nonTendererCriteria.map { it.id }}"
            )
        }
        AwardCriteria.COST_ONLY,
        AwardCriteria.QUALITY_ONLY,
        AwardCriteria.RATED_CRITERIA -> Unit
    }
}

fun checkCoefficientDataType(criteria: List<CriterionRequest>?, conversions: List<ConversionRequest>?) {
    fun mismatchDataTypeException(cv: CoefficientValue, rv: RequirementDataType): Nothing =
        throw ErrorException(
            ErrorType.INVALID_CONVERSION,
            message = "DataType in Conversion mismatch with Requirement dataType. $cv != $rv"
        )

    fun mismatchValueException(cv: CoefficientValue, rv: RequirementValue): Nothing =
        throw ErrorException(
            ErrorType.INVALID_CONVERSION,
            message = "Value in Conversion mismatch with Requirement value. Coefficient value $cv don't satisfies the requirements  ${rv.javaClass.name.split(
                "."
            ).last()} -> ${rv}"
        )

    fun RequirementDataType.validateDataType(coefficient: ConversionRequest.Coefficient) {
        when (coefficient.value) {
            is CoefficientValue.AsBoolean -> if (this != RequirementDataType.BOOLEAN)
                mismatchDataTypeException(coefficient.value, this)

            is CoefficientValue.AsString -> if (this != RequirementDataType.STRING)
                mismatchDataTypeException(coefficient.value, this)

            is CoefficientValue.AsNumber -> if (this != RequirementDataType.NUMBER)
                mismatchDataTypeException(coefficient.value, this)

            is CoefficientValue.AsInteger -> if (this != RequirementDataType.INTEGER)
                mismatchDataTypeException(coefficient.value, this)
        }
    }

    fun RequirementValue.validateValueCompatibility(
        coefficient: ConversionRequest.Coefficient,
        dataType: RequirementDataType
    ) {
        when (coefficient.value) {

            is CoefficientValue.AsBoolean -> if (this is ExpectedValue.AsBoolean && (coefficient.value.value != this.value))
                mismatchValueException(coefficient.value, this)

            is CoefficientValue.AsString -> Unit

            is CoefficientValue.AsNumber ->
                if (dataType == RequirementDataType.INTEGER
                    || (this is ExpectedValue.AsNumber && (coefficient.value.value.compareTo(this.value) != 0))
                    || (this is ExpectedValue.AsInteger && (coefficient.value.value.compareTo(BigDecimal(this.value)) != 0))

                    || (this is RangeValue.AsNumber && (coefficient.value.value.compareTo(this.minValue) == -1 || coefficient.value.value.compareTo(
                        this.maxValue
                    ) == 1))
                    || (this is RangeValue.AsInteger
                        && (coefficient.value.value < BigDecimal(this.minValue) || coefficient.value.value.compareTo(
                        BigDecimal(this.maxValue)
                    ) == 1))

                    || (this is MinValue.AsNumber && (coefficient.value.value.compareTo(this.value) == -1))
                    || (this is MinValue.AsInteger && (coefficient.value.value.compareTo(BigDecimal(this.value)) == -1))

                    || (this is MaxValue.AsNumber && (coefficient.value.value.compareTo(this.value) == 1))
                    || (this is MaxValue.AsInteger && (coefficient.value.value.compareTo(BigDecimal(this.value)) == 1))
                ) mismatchValueException(coefficient.value, this)

            is CoefficientValue.AsInteger ->
                if ((this is ExpectedValue.AsInteger && (coefficient.value.value != this.value))
                    || (this is ExpectedValue.AsNumber && (BigDecimal(coefficient.value.value).compareTo(this.value) != 0))

                    || ((this is RangeValue.AsInteger) && (coefficient.value.value < this.minValue || coefficient.value.value > this.maxValue))
                    || ((this is RangeValue.AsNumber)
                        && (BigDecimal(coefficient.value.value).compareTo(this.minValue) == -1 || BigDecimal(coefficient.value.value).compareTo(
                        this.maxValue
                    ) == 1))

                    || (this is MinValue.AsInteger && (coefficient.value.value < this.value))
                    || (this is MinValue.AsNumber && (BigDecimal(coefficient.value.value).compareTo(this.value) == -1))

                    || (this is MaxValue.AsInteger && (coefficient.value.value > this.value))
                    || (this is MaxValue.AsNumber && (BigDecimal(coefficient.value.value).compareTo(this.value) == 1))
                ) mismatchValueException(coefficient.value, this)
        }
    }

    if (criteria == null || conversions == null) return

    val requirements = criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .associateBy { it.id }

    val conversionsRelatesToRequirement = conversions.filter { it.relatesTo == ConversionsRelatesTo.REQUIREMENT }

    conversionsRelatesToRequirement.forEach {
        val requirementId = it.relatedItem
        it.coefficients.forEach { coefficient ->
            requirements.get(requirementId)
                ?.also { requirement ->
                    requirement.dataType.validateDataType(coefficient)
                }
        }
    }
}

val MAX_LIMIT_FOR_GOODS = 0.6.toBigDecimal()
val MAX_LIMIT_FOR_WORKS = 0.8.toBigDecimal()
val MAX_LIMIT_FOR_SERVICES = 0.4.toBigDecimal()

fun checkCastCoefficient(
    mainProcurementCategory: MainProcurementCategory?,
    criteria: List<CriterionRequest>?,
    conversions: List<ConversionRequest>?,
    items: List<ItemReferenceRequest>
) {
    fun castCoefficientException(limit: BigDecimal): Nothing =
        throw ErrorException(
            ErrorType.INVALID_CONVERSION,
            message = "cast coefficient in conversion cannot be greater than ${limit} "
        )

    if (criteria == null || conversions == null) return

    val castCoefficients = getCastCoefficients(criteria, conversions, items)

    when (mainProcurementCategory) {
        MainProcurementCategory.GOODS -> if (castCoefficients.any { it > MAX_LIMIT_FOR_GOODS })
            castCoefficientException(MAX_LIMIT_FOR_GOODS)

        MainProcurementCategory.WORKS -> if (castCoefficients.any { it > MAX_LIMIT_FOR_WORKS })
            castCoefficientException(MAX_LIMIT_FOR_WORKS)

        MainProcurementCategory.SERVICES -> if (castCoefficients.any { it > MAX_LIMIT_FOR_SERVICES })
            castCoefficientException(MAX_LIMIT_FOR_SERVICES)
    }
}

fun CriterionRequest.isCriteriaForTender(): Boolean = this.relatesTo == null
fun CriterionRequest.isCriteriaForLot(): Boolean = this.relatesTo == CriteriaRelatesToEnum.LOT
fun CriterionRequest.isCriteriaForItem(): Boolean = this.relatesTo == CriteriaRelatesToEnum.ITEM

fun getCastCoefficients(
    criteria: List<CriterionRequest>,
    conversions: List<ConversionRequest>,
    items: List<ItemReferenceRequest>
): List<BigDecimal> {

    fun Sequence<CriterionRequest>.getRelatedConversions(conversions: List<ConversionRequest>): Sequence<ConversionRequest> =
        this.flatMap { it.requirementGroups.asSequence() }
            .flatMap { it.requirements.asSequence() }
            .flatMap { requirement ->
                conversions
                    .asSequence()
                    .filter { it.relatedItem == requirement.id }
            }

    val filteredConversions = conversions.filter { it.relatesTo == ConversionsRelatesTo.REQUIREMENT }

    val tenderConversions = criteria.asSequence()
        .filter { it.isCriteriaForTender() }
        .getRelatedConversions(filteredConversions)
        .toList()

    val lots = items.toSetBy { it.relatedLot }
    val lotAndItemConversions = lots.map { lotId ->

        val lotConversions = criteria.asSequence()
            .filter { it.isCriteriaForLot() && it.relatedItem == lotId }
            .getRelatedConversions(filteredConversions)
            .toList()

        val relatedItems = items.asSequence()
            .filter { it.relatedLot == lotId }
            .map { it.id }

        val itemConversions = criteria.asSequence()
            .filter { it.isCriteriaForItem() && it.relatedItem in relatedItems }
            .getRelatedConversions(filteredConversions)
            .toList()

        lotConversions + itemConversions
    }

    return if (lotAndItemConversions.isEmpty())
        listOf(calculateCastCoefficient(tenderConversions))
    else
        lotAndItemConversions
            .map { calculateCastCoefficient(tenderConversions + it) }
}

fun getRequirementGroupsCombinations(
    criteria: List<CriterionRequest>,
    currentCriterionIndex: Int,
    combination: List<CriterionRequest.RequirementGroup>
): List<List<CriterionRequest.RequirementGroup>> {
    if (currentCriterionIndex == criteria.size)
        return listOf(combination)

    val finishedCombinations = mutableListOf<List<CriterionRequest.RequirementGroup>>()

    val criterionToAddToCombinationFrom = criteria[currentCriterionIndex]
    for (requirementGroup in criterionToAddToCombinationFrom.requirementGroups) {
        val newCombination = combination.toMutableList()
        newCombination.add(requirementGroup)
        finishedCombinations.addAll(
            getRequirementGroupsCombinations(
                criteria, currentCriterionIndex + 1, newCombination
            )
        )
    }

    return finishedCombinations
}

fun main(){ //TODO: delete
    val reqGroup1 =  CriterionRequest.RequirementGroup(id = "1", description = "", requirements = emptyList())
    val reqGroup2 =  CriterionRequest.RequirementGroup(id = "2", description = "", requirements = emptyList())

    val reqGroup3 =  CriterionRequest.RequirementGroup(id = "3", description = "", requirements = emptyList())
    val reqGroup4 =  CriterionRequest.RequirementGroup(id = "4", description = "", requirements = emptyList())
    val reqGroup5 =  CriterionRequest.RequirementGroup(id = "5", description = "", requirements = emptyList())

    val reqGroup6 =  CriterionRequest.RequirementGroup(id = "6", description = "", requirements = emptyList())
    val reqGroup7 =  CriterionRequest.RequirementGroup(id = "7", description = "", requirements = emptyList())


    val groupsByCriterion1 = CriterionRequest(id = "cr1", description = null, title = "", relatedItem = null, relatesTo = null, requirementGroups = listOf(reqGroup1, reqGroup2))
    val groupsByCriterion2 = CriterionRequest(id = "cr2", description = null, title = "", relatedItem = null, relatesTo = null, requirementGroups =  listOf(reqGroup3, reqGroup4, reqGroup5))
    val groupsByCriterion3 = CriterionRequest(id = "cr3", description = null, title = "", relatedItem = null, relatesTo = null, requirementGroups = listOf(reqGroup6, reqGroup7))

    val groupsByCriteria = listOf(groupsByCriterion1, groupsByCriterion2, groupsByCriterion3)

    val result = getRequirementGroupsCombinations(groupsByCriteria, 0, emptyList())

    println(result)
}


fun calculateCastCoefficient(conversions: List<ConversionRequest>): BigDecimal =
    conversions
        .map { conversion ->
            val minCoefficient = conversion.coefficients.minBy { it.coefficient.rate }!!.coefficient.rate
            BigDecimal.ONE - minCoefficient
        }
        .fold(BigDecimal.ZERO, java.math.BigDecimal::add)


fun checkConversionRelatesToEnum(conversions: List<ConversionRequest>?) {
    fun ConversionRequest.validate() {
        when (this.relatesTo) {
            ConversionsRelatesTo.REQUIREMENT -> Unit
        }
    }

    conversions?.forEach { it.validate() }
}

fun checkAwardCriteriaEnum(awardCriteria: AwardCriteria) {
    when (awardCriteria) {
        AwardCriteria.PRICE_ONLY,
        AwardCriteria.COST_ONLY,
        AwardCriteria.QUALITY_ONLY,
        AwardCriteria.RATED_CRITERIA -> Unit
    }
}

fun checkAwardCriteriaDetailsEnum(awardCriteriaDetails: AwardCriteriaDetails?) {
    when (awardCriteriaDetails) {
        AwardCriteriaDetails.MANUAL,
        AwardCriteriaDetails.AUTOMATED -> Unit
        null -> Unit
    }
}

inline fun <reified T, R> List<T>.validateUniqueness(uniques: Set<R>) {
    if (this.size != uniques.size) throw ErrorException(
        error = ErrorType.NOT_UNIQUE_IDS,
        message = "All elements in ${T::class.java.simpleName} arrays in json must be unique by id"
    )
}

inline fun <reified T> List<T>.validateNotEmpty() {
    if (this.isEmpty()) throw ErrorException(
        error = ErrorType.EMPTY_LIST,
        message = "All arrays of '${T::class.java.simpleName}' in json must have at least one object have to be added "
    )
}

fun checkItemArrays(items: List<ItemReferenceRequest>) {
    items.apply {
        validateNotEmpty()
        validateUniqueness(uniques = items.toSetBy { it.id })
    }
}

fun checkCriterionArrays(criteria: List<CriterionRequest>?) {
    if (criteria == null) return
    criteria.apply {
        validateNotEmpty()
        validateUniqueness(uniques = criteria.toSetBy { it.id })
    }
    criteria.forEach { criterion ->
        criterion.requirementGroups.apply {
            validateNotEmpty()
            validateUniqueness(uniques = criterion.requirementGroups.toSetBy { it.id })
        }

        criterion.requirementGroups.forEach { requirementGroup ->
            requirementGroup.requirements.apply {
                validateNotEmpty()
                validateUniqueness(uniques = requirementGroup.requirements.toSetBy { it.id })
            }
        }
    }
}

fun checkConversionArrays(conversions: List<ConversionRequest>?) {
    if (conversions == null) return
    conversions.apply {
        validateNotEmpty()
        validateUniqueness(uniques = conversions.toSetBy { it.id })
    }

    conversions.forEach { conversion ->
        conversion.coefficients.apply {
            validateNotEmpty()
            validateUniqueness(uniques = conversion.coefficients.toSetBy { it.id })
        }
    }
}

fun checkAwardCriteriaDetailsAreRequired(awardCriteria: AwardCriteria, awardCriteriaDetails: AwardCriteriaDetails?) {
    when (awardCriteria) {
        AwardCriteria.COST_ONLY,
        AwardCriteria.QUALITY_ONLY,
        AwardCriteria.RATED_CRITERIA -> if (awardCriteriaDetails == null)
            throw ErrorException(
                ErrorType.INVALID_AWARD_CRITERIA,
                message = "For awardCriteria in [" +
                    "${AwardCriteria.COST_ONLY}, " +
                    "${AwardCriteria.QUALITY_ONLY}, " +
                    "${AwardCriteria.RATED_CRITERIA}" +
                    "] field 'awardCriteriaDetails' are required "
            )
        AwardCriteria.PRICE_ONLY -> Unit
    }
}

fun checkCriteriaAndConversionAreRequired(
    awardCriteria: AwardCriteria,
    awardCriteriaDetails: AwardCriteriaDetails?,
    criteria: List<CriterionRequest>?,
    conversions: List<ConversionRequest>?
) {
    fun isNonPriceCriteria(awardCriteria: AwardCriteria) = when (awardCriteria) {
        AwardCriteria.COST_ONLY,
        AwardCriteria.QUALITY_ONLY,
        AwardCriteria.RATED_CRITERIA -> true
        AwardCriteria.PRICE_ONLY -> false
    }

    fun isAutomatedCriteria(awardCriteriaDetails: AwardCriteriaDetails?) = when (awardCriteriaDetails) {
        AwardCriteriaDetails.AUTOMATED -> true
        AwardCriteriaDetails.MANUAL -> false
        null -> false
    }

    if (isNonPriceCriteria(awardCriteria)
        && isAutomatedCriteria(awardCriteriaDetails)
        && (criteria == null || conversions == null)
    ) throw ErrorException(
        ErrorType.INVALID_AWARD_CRITERIA,
        message = "For awardCriteria in [" +
            "${AwardCriteria.COST_ONLY}, " +
            "${AwardCriteria.QUALITY_ONLY}, " +
            "${AwardCriteria.RATED_CRITERIA}" +
            "] && 'awardCriteriaDetails' in [${AwardCriteriaDetails.AUTOMATED}] Criteria and Conversion are required. "
    )
}

fun checkCoefficientRelatedOption(criteria: List<CriterionRequest>?, conversions: List<ConversionRequest>?) {
    val requirementsByIds = criteria
        ?.asSequence()
        ?.flatMap { criterion -> criterion.requirementGroups.asSequence() }
        ?.flatMap { requirementGroup -> requirementGroup.requirements.asSequence() }
        ?.associateBy { requirement -> requirement.id }
        ?: kotlin.collections.emptyMap()

    conversions
        ?.asSequence()
        ?.filter { conversion -> conversion.relatesTo == ConversionsRelatesTo.REQUIREMENT }
        ?.forEach { conversion ->
            val requirement = requirementsByIds[conversion.relatedItem]
                ?: throw ErrorException(
                    error = ErrorType.INVALID_CONVERSION,
                    message = "The conversion '${conversion.id}' related with unknown the requirement '${conversion.relatedItem}'."
                )

            if (requirement.dataType == RequirementDataType.STRING) {
                conversion.coefficients
                    .forEach { coefficient ->
                        if (coefficient.relatedOption == null)
                            throw ErrorException(
                                error = ErrorType.INVALID_COEFFICIENT,
                                message = "The coefficient '${coefficient.id}' which is related to the requirement '${conversion.relatedItem}' of data type '${requirement.dataType}' does not contain attribute 'relatedOption'."
                            )
                    }
            }
        }
}
