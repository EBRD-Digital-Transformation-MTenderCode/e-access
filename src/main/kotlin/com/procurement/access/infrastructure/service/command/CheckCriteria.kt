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
import com.procurement.access.infrastructure.dto.cn.OpenCnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.criteria.ExpectedValue
import com.procurement.access.infrastructure.dto.cn.criteria.MaxValue
import com.procurement.access.infrastructure.dto.cn.criteria.MinValue
import com.procurement.access.infrastructure.dto.cn.criteria.NoneValue
import com.procurement.access.infrastructure.dto.cn.criteria.RangeValue
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.dto.cn.criteria.RequirementValue
import java.math.BigDecimal

fun OpenCnOnPnRequest.checkConversionWithoutCriteria(): OpenCnOnPnRequest {
    val tender = this.tender
    if (tender.criteria == null && tender.conversions != null)
        throw ErrorException(
            ErrorType.INVALID_CONVERSION,
            message = "Conversions cannot exists without criteria"
        )
    return this
}

fun OpenCnOnPnRequest.checkActualItemRelation(): OpenCnOnPnRequest {
    fun Map<String, OpenCnOnPnRequest.Item>.containsElement(itemId: String) {
        if (!this.containsKey(itemId)) throw ErrorException(
            ErrorType.INVALID_CRITERIA,
            message = "Criteria relates to item that does not exists. Item id=${itemId}, Available items: ${this.keys}"
        )
    }

    fun Map<String, List<OpenCnOnPnRequest.Item>>.relatesWithLot(lotId: String) {
        if (!this.containsKey(lotId)) throw ErrorException(
            ErrorType.INVALID_CRITERIA,
            message = "Criteria relates to lot that does not exists. Item id=${lotId}, Available lots: ${this.keys}"
        )
    }

    fun OpenCnOnPnRequest.Tender.Criteria.validate(
        itemsById: Map<String, OpenCnOnPnRequest.Item>,
        itemsByRelatedLot: Map<String, List<OpenCnOnPnRequest.Item>>
    ) {
        when (this.relatesTo) {
            CriteriaRelatesToEnum.ITEM     -> itemsById.containsElement(this.relatedItem!!)
            CriteriaRelatesToEnum.LOT      -> itemsByRelatedLot.relatesWithLot(this.relatedItem!!)
            CriteriaRelatesToEnum.TENDERER -> Unit
        }
    }

    fun OpenCnOnPnRequest.Tender.Criteria.validateRelation() {
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
                CriteriaRelatesToEnum.LOT      -> if (this.relatedItem == null) throw ErrorException(
                    error = ErrorType.INVALID_CRITERIA,
                    message = "For parameter relatedTo = 'lot' or 'item', parameter relatedItem must be specified"
                )
            }
        }
    }

    val criteria = this.tender.criteria ?: return this
    val items = this.items

    val itemsById = items.associateBy { it.id }
    val itemsByRelatedLot = items.groupBy { it.relatedLot }

    criteria.forEach { _criteria ->
        _criteria.validateRelation()
        _criteria.validate(itemsById, itemsByRelatedLot)
    }

    return this
}

fun OpenCnOnPnRequest.checkDatatypeCompliance(): OpenCnOnPnRequest {
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

            is ExpectedValue.AsString  -> if (this.dataType != RequirementDataType.STRING)
                mismatchDatatypeException(this.value, this.dataType)

            is ExpectedValue.AsInteger,
            is MinValue.AsInteger,
            is MaxValue.AsInteger,
            is RangeValue.AsInteger    -> if (this.dataType != RequirementDataType.INTEGER)
                mismatchDatatypeException(this.value, this.dataType)

            is ExpectedValue.AsNumber,
            is MinValue.AsNumber,
            is MaxValue.AsNumber,
            is RangeValue.AsNumber     -> if (this.dataType != RequirementDataType.NUMBER)
                mismatchDatatypeException(this.value, this.dataType)
        }
    }

    val criteria = this.tender.criteria ?: return this

    criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .forEach { it.validate() }

    return this
}

fun OpenCnOnPnRequest.checkMinMaxValue(): OpenCnOnPnRequest {
    fun rangeException(): Nothing = throw ErrorException(
        ErrorType.INVALID_REQUIREMENT_VALUE,
        message = "minValue greater than or equals to maxValue"
    )

    fun <T : Number> validateRange(minValue: T, maxValue: T) {
        when (minValue) {
            is Long       -> if (minValue >= maxValue.toLong()) rangeException()
            is BigDecimal -> if (minValue >= java.math.BigDecimal(maxValue.toString())) rangeException()
        }
    }

    fun RangeValue.validate() {
        when (this) {
            is RangeValue.AsNumber  -> validateRange(minValue = this.minValue, maxValue = this.maxValue)
            is RangeValue.AsInteger -> validateRange(minValue = this.minValue, maxValue = this.maxValue)
        }
    }

    val criteria = this.tender.criteria ?: return this

    criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .forEach { if (it.value is RangeValue) it.value.validate() }

    return this
}

fun OpenCnOnPnRequest.checkDateTime(): OpenCnOnPnRequest {
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

    val criteria = this.tender.criteria ?: return this

    criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .forEach { it.period?.validate() }

    return this
}

fun OpenCnOnPnRequest.checkRequirements(): OpenCnOnPnRequest {

    val criteria = this.tender.criteria ?: return this

    val criteriaById = criteria.groupBy { it.id }
    val requirementGroupById = criteria.flatMap { it.requirementGroups }
        .groupBy { it.id }
    val requirementById = criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .groupBy { it.id }

    fun OpenCnOnPnRequest.Tender.Criteria.validateId() {
        val criteriaId = criteriaById.get(this.id)
        if (criteriaId != null && criteriaId.size > 1)
            throw ErrorException(
                ErrorType.INVALID_CRITERIA,
                message = "criteria.id is not unique. Current id = ${this.id}, All ids: ${criteriaById}"
            )
    }

    fun OpenCnOnPnRequest.Tender.Criteria.RequirementGroup.validateId() {
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

    fun List<OpenCnOnPnRequest.Tender.Criteria.RequirementGroup>.validateRequirementGroupCount() {
        if (this.size == 0)
            throw ErrorException(
                ErrorType.INVALID_CRITERIA,
                message = "Must be at least one requirementGroup have to be added"
            )
    }

    fun List<Requirement>.validateRequirementsCount() {
        if (this.size == 0)
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

    return this
}

fun OpenCnOnPnRequest.checkConversionRelation(): OpenCnOnPnRequest {

    val criteria = this.tender.criteria ?: return this
    val conversions = this.tender.conversions ?: return this

    val requirements = criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .groupBy { it.id }

    val relation: MutableMap<String, List<Requirement>> = kotlin.collections.mutableMapOf()
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

    return this
}

fun OpenCnOnPnRequest.checkCoefficient(): OpenCnOnPnRequest {
    val COEFFICIENT_MIN = 0.01.toBigDecimal()
    val COEFFICIENT_MAX = 1.toBigDecimal()

    fun OpenCnOnPnRequest.Tender.Conversion.Coefficient.validateCoefficientRate() {
        if (this.coefficient.rate < COEFFICIENT_MIN || this.coefficient.rate > COEFFICIENT_MAX)
            throw ErrorException(
                ErrorType.INVALID_CONVERSION,
                message = "Conversion coefficient rate (${this.coefficient}) does not satisfy the conditions: " +
                    "coefficient in [${COEFFICIENT_MIN}, ${COEFFICIENT_MAX}]"
            )
    }

    val conversions = this.tender.conversions ?: return this

    conversions
        .flatMap { it.coefficients }
        .forEach { it.validateCoefficientRate() }

    return this
}

fun OpenCnOnPnRequest.checkCoefficientValueUniqueness(): OpenCnOnPnRequest {
    fun uniquenessException(coefficients: List<OpenCnOnPnRequest.Tender.Conversion.Coefficient>): Nothing = throw ErrorException(
        ErrorType.INVALID_CONVERSION,
        message = "Conversion coefficients value contains not unique element: " +
            "${coefficients.map { it.value }
                .map { if (it is CoefficientValue.AsNumber) it.value.stripTrailingZeros() else it }
                .groupBy { it }
                .filter { it.value.size > 1 }.keys}"
    )

    fun List<OpenCnOnPnRequest.Tender.Conversion.Coefficient>.validateCoefficientValues() {
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

    val conversions = this.tender.conversions ?: return this

    conversions.forEach { conversion ->
        conversion.coefficients.validateCoefficientValues()
    }

    return this
}

fun OpenCnOnPnRequest.checkCriteriaWithAwardCriteria(): OpenCnOnPnRequest {
    if (this.tender.criteria == null) return this

    when (this.tender.awardCriteria) {
        AwardCriteria.PRICE_ONLY     -> {
            if (this.tender.conversions != null) throw ErrorException(
                error = ErrorType.INVALID_CONVERSION,
                message = "For awardCriteria='priceOnly' conversion cannot be passed"
            )

            val nonTendererCriteria = this.tender.criteria.filter { it.relatesTo != CriteriaRelatesToEnum.TENDERER }
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

    return this
}

fun OpenCnOnPnRequest.checkCoefficientDataType(): OpenCnOnPnRequest {
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

    fun RequirementDataType.validateDataType(coefficient: OpenCnOnPnRequest.Tender.Conversion.Coefficient) {
        when (coefficient.value) {
            is CoefficientValue.AsBoolean -> if (this != RequirementDataType.BOOLEAN)
                mismatchDataTypeException(coefficient.value, this)

            is CoefficientValue.AsString  -> if (this != RequirementDataType.STRING)
                mismatchDataTypeException(coefficient.value, this)

            is CoefficientValue.AsNumber  -> if (this != RequirementDataType.NUMBER)
                mismatchDataTypeException(coefficient.value, this)

            is CoefficientValue.AsInteger -> if (this != RequirementDataType.INTEGER)
                mismatchDataTypeException(coefficient.value, this)
        }
    }

    fun RequirementValue.validateValueCompatibility(
        coefficient: OpenCnOnPnRequest.Tender.Conversion.Coefficient,
        dataType: RequirementDataType
    ) {
        when (coefficient.value) {

            is CoefficientValue.AsBoolean -> if (this is ExpectedValue.AsBoolean && (coefficient.value.value != this.value))
                mismatchValueException(coefficient.value, this)

            is CoefficientValue.AsString  -> Unit

            is CoefficientValue.AsNumber  ->
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

    val criteria = this.tender.criteria ?: return this
    val conversions = this.tender.conversions ?: return this

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

    return this
}

fun OpenCnOnPnRequest.checkCastCoefficient(): OpenCnOnPnRequest {
    fun castCoefficientException(limit: BigDecimal): Nothing =
        throw ErrorException(
            ErrorType.INVALID_CONVERSION,
            message = "cast coefficient in conversion cannot be greater than ${limit} "
        )

    val criteria: List<OpenCnOnPnRequest.Tender.Criteria> = this.tender.criteria ?: return this
    val requestConversions: List<OpenCnOnPnRequest.Tender.Conversion> = this.tender.conversions ?: return this

    val conversions: List<OpenCnOnPnRequest.Tender.Conversion> = requestConversions.filter { it.relatesTo == ConversionsRelatesTo.REQUIREMENT }
    val items: List<OpenCnOnPnRequest.Item> = this.items

    val tenderRequirements = criteria.asSequence()
        .filter { it.relatesTo == null }
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .toList()

    val tenderConversions = tenderRequirements.flatMap { requirement ->
        conversions.filter { it.relatedItem == requirement.id }
    }

    fun OpenCnOnPnRequest.Tender.Criteria.getRelatedItems(items: List<OpenCnOnPnRequest.Item>) =
        items.filter { it.relatedLot == this.relatedItem }

    val criteriaRelatedToLot = criteria.filter { it.relatesTo == CriteriaRelatesToEnum.LOT }
    val criteriaRelatedToItem = criteria.filter { it.relatesTo == CriteriaRelatesToEnum.ITEM }

    criteriaRelatedToLot.forEach { lotCriteria ->
        val lotRequirement = lotCriteria.requirementGroups
            .flatMap { it.requirements }
        val lotConversions = lotRequirement.flatMap { requirement ->
            conversions.filter { it.relatedItem == requirement.id }
        }

        val relatedItems = lotCriteria.getRelatedItems(items)

        val itemRequirement = relatedItems.flatMap { item ->
            criteriaRelatedToItem.asSequence()
                .filter { it.relatedItem == item.id }
                .flatMap { it.requirementGroups.asSequence() }
                .flatMap { it.requirements.asSequence() }
                .toList()
        }
        val itemConversions = itemRequirement.flatMap { requirement ->
            conversions.filter { it.relatedItem == requirement.id }
        }

        val castCoefficient = (tenderConversions + lotConversions + itemConversions)
            .map { conversion -> java.math.BigDecimal(1) - conversion.coefficients.minBy { it.coefficient.rate }!!.coefficient.rate }
            .fold(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)

        val mainProcurementCategory = this.mainProcurementCategory

        val MAX_LIMIT_FOR_GOODS = 0.6.toBigDecimal()
        val MAX_LIMIT_FOR_WORKS = 0.8.toBigDecimal()
        val MAX_LIMIT_FOR_SERVICES = 0.4.toBigDecimal()

        when (mainProcurementCategory) {
            MainProcurementCategory.GOODS    -> if (castCoefficient > MAX_LIMIT_FOR_GOODS)
                castCoefficientException(MAX_LIMIT_FOR_GOODS)

            MainProcurementCategory.WORKS    -> if (castCoefficient > MAX_LIMIT_FOR_WORKS)
                castCoefficientException(MAX_LIMIT_FOR_WORKS)

            MainProcurementCategory.SERVICES -> if (castCoefficient > MAX_LIMIT_FOR_SERVICES)
                castCoefficientException(MAX_LIMIT_FOR_SERVICES)
        }
    }

    return this
}

fun OpenCnOnPnRequest.checkConversionRelatesToEnum(): OpenCnOnPnRequest {
    fun OpenCnOnPnRequest.Tender.Conversion.validate() {
        when (this.relatesTo) {
            ConversionsRelatesTo.REQUIREMENT -> Unit
        }
    }

    val tender = this.tender
    val conversions = tender.conversions ?: return this
    conversions.forEach { it.validate() }
    return this
}

fun OpenCnOnPnRequest.checkAwardCriteriaEnum(): OpenCnOnPnRequest {
    val tender = this.tender
    when (tender.awardCriteria) {
        AwardCriteria.PRICE_ONLY,
        AwardCriteria.COST_ONLY,
        AwardCriteria.QUALITY_ONLY,
        AwardCriteria.RATED_CRITERIA -> Unit
    }
    return this
}

fun OpenCnOnPnRequest.checkAwardCriteriaDetailsEnum(): OpenCnOnPnRequest {
    when (this.tender.awardCriteriaDetails) {
        AwardCriteriaDetails.MANUAL,
        AwardCriteriaDetails.AUTOMATED -> Unit
    }
    return this
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

fun OpenCnOnPnRequest.checkArrays(): OpenCnOnPnRequest {

    val tender = this.tender

    this.items.apply {
        validateNotEmpty()
        validateUniqueness(uniques = items.toSetBy { it.id })
    }

    tender.criteria?.let { criterias ->
        criterias.apply {
            validateNotEmpty()
            validateUniqueness(uniques = criterias.toSetBy { it.id })
        }
        criterias.forEach { criteria ->
            criteria.requirementGroups.apply {
                validateNotEmpty()
                validateUniqueness(uniques = criteria.requirementGroups.toSetBy { it.id })
            }

            criteria.requirementGroups.forEach { requirementGroup ->
                requirementGroup.requirements.apply {
                    validateNotEmpty()
                    validateUniqueness(uniques = requirementGroup.requirements.toSetBy { it.id })
                }
            }
        }
    }

    tender.conversions?.let { conversions ->
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

    return this
}

fun OpenCnOnPnRequest.checkAwardCriteriaDetailsAreRequired(): OpenCnOnPnRequest {
    val tender = this.tender

    when (tender.awardCriteria) {
        AwardCriteria.COST_ONLY,
        AwardCriteria.QUALITY_ONLY,
        AwardCriteria.RATED_CRITERIA -> if (tender.awardCriteriaDetails == null)
            throw ErrorException(
                ErrorType.INVALID_AWARD_CRITERIA,
                message = "For awardCriteria in [" +
                    "${AwardCriteria.COST_ONLY}, " +
                    "${AwardCriteria.QUALITY_ONLY}, " +
                    "${AwardCriteria.RATED_CRITERIA}" +
                    "] field 'awardCriteriaDetails' are required "
            )
        AwardCriteria.PRICE_ONLY     -> Unit
    }

    return this
}

fun OpenCnOnPnRequest.checkCriteriaAndConversionAreRequired(): OpenCnOnPnRequest {

    val tender = this.tender

    fun isNonPriceCriteria() = when (tender.awardCriteria) {
        AwardCriteria.COST_ONLY,
        AwardCriteria.QUALITY_ONLY,
        AwardCriteria.RATED_CRITERIA -> true
        AwardCriteria.PRICE_ONLY     -> false
        null                         -> false
    }

    fun isAutomatedCriteria() = when (tender.awardCriteriaDetails) {
        AwardCriteriaDetails.AUTOMATED -> true
        AwardCriteriaDetails.MANUAL    -> false
        null                           -> false
    }

    if (isNonPriceCriteria()
        && isAutomatedCriteria()
        && (tender.criteria == null || tender.conversions == null)
    ) throw ErrorException(
        ErrorType.INVALID_AWARD_CRITERIA,
        message = "For awardCriteria in [" +
            "${AwardCriteria.COST_ONLY}, " +
            "${AwardCriteria.QUALITY_ONLY}, " +
            "${AwardCriteria.RATED_CRITERIA}" +
            "] && 'awardCriteriaDetails' in [${AwardCriteriaDetails.AUTOMATED}] Criteria and Conversion are required. "
    )

    return this
}

fun OpenCnOnPnRequest.checkCoefficientRelatedOption(): OpenCnOnPnRequest {
    val requirementsByIds = tender.criteria
        ?.asSequence()
        ?.flatMap { criteria -> criteria.requirementGroups.asSequence() }
        ?.flatMap { requirementGroup -> requirementGroup.requirements.asSequence() }
        ?.associateBy { requirement -> requirement.id }
        ?: kotlin.collections.emptyMap()

    tender.conversions
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

    return this
}