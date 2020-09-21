package com.procurement.access.service

import com.procurement.access.application.model.context.CheckFEDataContext
import com.procurement.access.application.service.fe.check.CheckFEDataData
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.criteria.ExpectedValue
import com.procurement.access.infrastructure.dto.cn.criteria.MaxValue
import com.procurement.access.infrastructure.dto.cn.criteria.MinValue
import com.procurement.access.infrastructure.dto.cn.criteria.NoneValue
import com.procurement.access.infrastructure.dto.cn.criteria.RangeValue
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.dto.cn.criteria.RequirementValue
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

interface FeValidationService {
    fun checkFEData(context: CheckFEDataContext, data: CheckFEDataData)
}

@Service
class FeValidationServiceImpl(private val tenderProcessDao: TenderProcessDao) : FeValidationService {

    companion object CheckFEData {

        fun validateTitle(title: String) {
            // VR-1.0.1.1.7
            if (title.isBlank())
                throw ErrorException(
                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                    message = "Tender.title should contain at least one character"
                )
        }

        fun validateDescription(description: String) {
            // VR-1.0.1.1.8
            if (description.isBlank())
                throw ErrorException(
                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                    message = "Tender.description should contain at least one character"
                )
        }

        private fun validateProcuringEntityId(receivedId: String, storedId: String) {
            if (receivedId != storedId)
                throw ErrorException(
                    error = ErrorType.INVALID_PROCURING_ENTITY,
                    message = "Invalid identifier of procuring entity"
                )
        }

        fun <T> validateUniquenessBy(elements: Collection<T>, collectionName: String, selector: (T) -> String) {
            val selectedElements = elements.map { element ->
                selector(element)
            }
            val uniqSelectedElements = selectedElements.toSet()
            if (selectedElements.size != uniqSelectedElements.size)
                throw ErrorException(
                    error = ErrorType.NOT_UNIQUE_IDS,
                    message = "$collectionName contains duplicated ids."
                )
        }

        fun validatePersonsExistence(persons: List<CheckFEDataData.Tender.ProcuringEntity.Person>) {
            if (persons.isEmpty())
                throw ErrorException(
                    error = ErrorType.INVALID_PERSON,
                    message = "At least one Person should be added"
                )
        }

        fun validateProcuringEntity(
            operationType: OperationType,
            entity: TenderProcessEntity,
            procuringEntity: CheckFEDataData.Tender.ProcuringEntity
        ) {

            when (operationType) {
                OperationType.AMEND_FE -> {
                    val fe = toObject(FEEntity::class.java, entity.jsonData)

                    // VR-1.0.1.10.1
                    fe.tender.procuringEntity?.let { validateProcuringEntityId(procuringEntity.id, it.id) }
                }
                OperationType.CREATE_FE -> {
                    val ap = toObject(APEntity::class.java, entity.jsonData)

                    // VR-1.0.1.10.1
                    validateProcuringEntityId(procuringEntity.id, ap.tender.procuringEntity.id)
                }

                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_SUBMISSION,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMISSION_PERIOD_END,
                OperationType.TENDER_PERIOD_END,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> throw ErrorException(ErrorType.INVALID_PMD)
            }
        }

        fun validateTenderDocumentsExistance(documents: List<CheckFEDataData.Tender.Document>) {
            if (documents.isEmpty())
                throw ErrorException(
                    error = ErrorType.EMPTY_DOCS,
                    message = "At least one document should be added"
                )
        }

        fun validatePersonDocumentsExistance(documents: List<CheckFEDataData.Tender.ProcuringEntity.Person.BusinessFunction.Document>) {
            if (documents.isEmpty())
                throw ErrorException(
                    error = ErrorType.EMPTY_DOCS,
                    message = "At least one document should be added"
                )
        }

        fun validatePersonsBusinessFunctions(persons: List<CheckFEDataData.Tender.ProcuringEntity.Person>) {
            persons.forEach { person ->
                if (person.businessFunctions.isEmpty())
                    throw ErrorException(
                        error = ErrorType.INVALID_BUSINESS_FUNCTION,
                        message = "At least one businessFunctions detalization should be added"
                    )
            }
        }

        fun validateBusinessFunctionsPeriod(
            startDateFromContext: LocalDateTime,
            period: CheckFEDataData.Tender.ProcuringEntity.Person.BusinessFunction.Period
        ) {
            if (period.startDate > startDateFromContext)
                throw ErrorException(
                    error = ErrorType.INVALID_PERIOD_VALUE,
                    message = "Invalid period in bussiness function specification"
                )
        }

        fun validateSecondStageAttributesExistance(secondStage: CheckFEDataData.Tender.SecondStage) {
            if (secondStage.minimumCandidates == null && secondStage.maximumCandidates == null)
                throw ErrorException(
                    error = ErrorType.MISSING_ATTRIBUTE,
                    message = "At least one value should be: minimumCandidates or maximumCandidates"
                )
        }

        fun validateMinimumCandidates(candidatesAmount: Int) {
            if (candidatesAmount <= 0)
                throw ErrorException(
                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                    message = "MinimumCandidates value should not be zero"
                )
        }

        fun validateMaximumCandidates(candidatesAmount: Int) {
            if (candidatesAmount <= 0)
                throw ErrorException(
                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                    message = "MaximumCandidates value should not be zero"
                )
        }

        fun validateCandidates(secondStage: CheckFEDataData.Tender.SecondStage) {
            if (secondStage.minimumCandidates == null || secondStage.maximumCandidates == null) return
            if (secondStage.minimumCandidates >= secondStage.maximumCandidates)
                throw ErrorException(
                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                    message = "MaximumCandidates value should not be zero"
                )
        }

        fun checkOtherCriteria(otherCriteria: CheckFEDataData.Tender.OtherCriteria?) {
            if (otherCriteria == null)
                throw ErrorException(
                    error = ErrorType.MISSING_ATTRIBUTE,
                    message = "OtherCriteria should be in request"
                )
        }

        fun checkDatatypeCompliance(criteria: List<CheckFEDataData.Tender.Criteria>) {
            fun mismatchDatatypeException(rv: RequirementValue?, rDatatype: RequirementDataType): Nothing =
                throw ErrorException(
                    ErrorType.INVALID_CRITERIA,
                    message = "Requirement.dataType mismatch with datatype in expectedValue || minValue || maxValue. " +
                        "$rv != $rDatatype"
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

                    NoneValue -> Unit
                }
            }

            criteria.asSequence()
                .flatMap { it.requirementGroups.asSequence() }
                .flatMap { it.requirements.asSequence() }
                .forEach { it.validate() }
        }

        fun checkMinMaxValue(criteria: List<CheckFEDataData.Tender.Criteria>) {
            fun rangeException(): Nothing = throw ErrorException(
                ErrorType.INVALID_REQUIREMENT_VALUE,
                message = "minValue greater than or equals to maxValue"
            )

            fun <T : Number> validateRange(minValue: T, maxValue: T) {
                when (minValue) {
                    is Long -> if (minValue >= maxValue.toLong()) rangeException()
                    is BigDecimal -> if (minValue >= BigDecimal(maxValue.toString())) rangeException()
                }
            }

            fun RangeValue.validate() {
                when (this) {
                    is RangeValue.AsNumber -> validateRange(minValue = this.minValue, maxValue = this.maxValue)
                    is RangeValue.AsInteger -> validateRange(minValue = this.minValue, maxValue = this.maxValue)
                }
            }

            criteria.asSequence()
                .flatMap { it.requirementGroups.asSequence() }
                .flatMap { it.requirements.asSequence() }
                .forEach { if (it.value is RangeValue) it.value.validate() }
        }

        fun checkDateTime(criteria: List<CheckFEDataData.Tender.Criteria>) {
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            fun Requirement.Period.validate() {
                if (this.startDate.year > currentYear || this.endDate.year > currentYear)
                    throw ErrorException(
                        ErrorType.INVALID_PERIOD_VALUE,
                        message = "start/endDate year cannot be greater than current year. " +
                            "StartDate=${this.startDate}, " +
                            "EndDate=${this.endDate}, " +
                            "Current year = $currentYear"
                    )
                if (this.startDate > this.endDate)
                    throw ErrorException(
                        ErrorType.INVALID_PERIOD_VALUE,
                        message = "startDate cannot be greater than endDate. StartDate=${this.startDate}, EndDate=${this.endDate}"
                    )
            }

            criteria.asSequence()
                .flatMap { it.requirementGroups.asSequence() }
                .flatMap { it.requirements.asSequence() }
                .forEach { it.period?.validate() }
        }

        fun checkRequirements(criteria: List<CheckFEDataData.Tender.Criteria>) {
            fun List<CheckFEDataData.Tender.Criteria.RequirementGroup>.validateRequirementGroupCount() {
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


            criteria.forEach { _criteria ->
                _criteria.requirementGroups.validateRequirementGroupCount()

                // FReq-1.1.1.16
                validateUniquenessBy(_criteria.requirementGroups, "tender.criteria[].requirementGroup[]") { it.id }

                _criteria.requirementGroups.forEach { rg ->
                    rg.requirements.validateRequirementsCount()

                    // FReq-1.1.1.16
                    validateUniquenessBy(rg.requirements, "tender.criteria[].requirementGroup[].requirement[]") { it.id }
                }
            }
        }

        fun getEntity(tenderProcessDao: TenderProcessDao, context: CheckFEDataContext): TenderProcessEntity {
            val cpid = context.cpid
            val stage = when (context.operationType) {
                OperationType.AMEND_FE -> context.stage
                OperationType.CREATE_FE -> context.prevStage

                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_SUBMISSION,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMISSION_PERIOD_END,
                OperationType.TENDER_PERIOD_END,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> throw ErrorException(ErrorType.INVALID_PMD)
            }
            return tenderProcessDao.getByCpIdAndStage(cpId = cpid, stage = stage)
                ?: throw ErrorException(
                    error = ErrorType.ENTITY_NOT_FOUND,
                    message = "Cannot found tender (cpid='${cpid}' and stage='${stage}')"
                )
        }
    }

    override fun checkFEData(context: CheckFEDataContext, data: CheckFEDataData) {

        // FR.COM-1.27.1
        validateTitle(data.tender.title)

        // FR.COM-1.27.2
        validateDescription(data.tender.description)

        // FR.COM-1.27.3
        data.tender.procuringEntity?.let { procuringEntity ->
            val entity = getEntity(tenderProcessDao, context)

            // VR-1.0.1.10.1
            validateProcuringEntity(context.operationType, entity, data.tender.procuringEntity)

            // VR-1.0.1.10.2
            validatePersonsExistence(procuringEntity.persons)

            // FReq-1.1.1.16
            // VR-1.0.1.10.3
            validateUniquenessBy(procuringEntity.persons, "procuringEntity.persones[]") { it.id }

            // VR-1.0.1.10.4
            validatePersonsBusinessFunctions(procuringEntity.persons)

            procuringEntity.persons.forEach { person ->

                // FReq-1.1.1.16
                // VR-1.0.1.10.5
                validateUniquenessBy(person.businessFunctions, "procuringEntity.persones.businessFunctions") { it.id }

                person.businessFunctions.forEach { businessFunction ->
                    // VR-1.0.1.10.7
                    validateBusinessFunctionsPeriod(context.startDate, businessFunction.period)

                    // FReq-1.1.1.16
                    // VR-1.0.1.2.1
                    validateUniquenessBy(businessFunction.documents, "tender.businessFunctions[].documents[]") { it.id }

                    // VR-1.0.1.2.7
                    validatePersonDocumentsExistance(businessFunction.documents)
                }
            }
        }

        // FR.COM-1.27.4
        validateTenderDocumentsExistance(data.tender.documents)     // VR-1.0.1.2.7

        // FReq-1.1.1.16
        validateUniquenessBy(data.tender.documents, "tender.documents[]") { it.id }  // VR-1.0.1.2.1

        // FR.COM-1.27.5
        data.tender.secondStage?.let { secondStage ->
            // VR-1.0.1.11.1
            validateSecondStageAttributesExistance(secondStage)

            // VR-1.0.1.11.2
            secondStage.minimumCandidates?.let { validateMinimumCandidates(it) }

            // VR-1.0.1.11.3
            secondStage.maximumCandidates?.let { validateMaximumCandidates(it) }

            // VR-1.0.1.11.4
            validateCandidates(secondStage)
        }

        // FR.COM-1.27.6
        data.tender.criteria
            .takeIf { it.isNotEmpty() }
            ?.let { criteria ->

                // FReq-1.1.1.16
                validateUniquenessBy(criteria, "tender.criteria[]") { it.id }

                // FReq-1.1.1.4
                checkDatatypeCompliance(criteria)

                // FReq-1.1.1.5
                checkMinMaxValue(criteria)

                // FReq-1.1.1.6
                checkDateTime(criteria)

                // FReq-1.1.1.8
                checkRequirements(criteria)
            }

        // FR.COM-1.27.7
        // VR-1.0.1.12.1
        checkOtherCriteria(data.tender.otherCriteria)
    }
}
