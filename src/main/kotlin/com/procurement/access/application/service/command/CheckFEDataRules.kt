package com.procurement.access.application.service.command

import com.procurement.access.application.model.context.CheckFEDataContext
import com.procurement.access.application.service.fe.check.CheckFEDataData
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.requirement.EligibleEvidence
import com.procurement.access.domain.model.requirement.EligibleEvidenceType
import com.procurement.access.domain.model.requirement.ExpectedValue
import com.procurement.access.domain.model.requirement.MaxValue
import com.procurement.access.domain.model.requirement.MinValue
import com.procurement.access.domain.model.requirement.NoneValue
import com.procurement.access.domain.model.requirement.RangeValue
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.domain.model.requirement.RequirementValue
import com.procurement.access.domain.model.requirement.hasInvalidScale
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.lib.extension.isUnique
import com.procurement.access.lib.extension.toSet
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toObject
import java.math.BigDecimal
import java.time.LocalDateTime

class CheckFEDataRules {
    companion object {

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
            if (!elements.isUnique(selector))
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
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
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
                    message = "MaximumCandidates value should be more or equal MinimumCandidates"
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


            criteria.forEach { criterion ->
                criterion.requirementGroups.validateRequirementGroupCount()

                // FReq-1.1.1.16
                validateUniquenessBy(criterion.requirementGroups, "tender.criteria[].requirementGroup[]") { it.id }

                criterion.requirementGroups.forEach { rg ->
                    rg.requirements.validateRequirementsCount()

                    // FReq-1.1.1.16
                    validateUniquenessBy(
                        rg.requirements,
                        "tender.criteria[].requirementGroup[].requirement[]"
                    ) { it.id }
                }
            }
        }

        fun checkCriteriaRelation(relations: List<CriteriaRelatesTo>): Unit =
            relations.forEach { relation ->
                when (relation) {
                    CriteriaRelatesTo.TENDERER -> Unit
                    CriteriaRelatesTo.AWARD,
                    CriteriaRelatesTo.ITEM,
                    CriteriaRelatesTo.LOT,
                    CriteriaRelatesTo.QUALIFICATION,
                    CriteriaRelatesTo.TENDER ->
                        throw ErrorException(
                            ErrorType.INVALID_CRITERIA,
                            message = "Criteria can relates only to '${CriteriaRelatesTo.TENDERER}'."
                        )
                }
            }

        fun checkCriteriaCategory(criteria: List<CheckFEDataData.Tender.Criteria>): Unit =
            criteria.forEach { criteria ->
                if(!isOfExclusionCategory(criteria.classification.id) &&
                    !isOfSelectionCategory(criteria.classification.id))
                        throw ErrorException(
                            ErrorType.INVALID_CRITERIA,
                            message = "Invalid criteria category."
                        )

            }

        private fun isOfSelectionCategory(criteriaClassificationId: String) =
            criteriaClassificationId.startsWith("CRITERION.SELECTION.")

        private fun isOfExclusionCategory(criteriaClassificationId: String) =
            criteriaClassificationId.startsWith("CRITERION.EXCLUSION.")

        fun checkClassification(
            tenderCriteria: List<CheckFEDataData.Tender.Criteria>,
            criteria: List<CheckFEDataData.Criterion>
        ) {
            if (tenderCriteria.isEmpty()) return

            checkExclusionClassification(tenderCriteria, criteria)
            checkSelectionClassification(tenderCriteria, criteria)
        }

        fun checkEligibleEvidences(tenderCriteria: List<CheckFEDataData.Tender.Criteria>, documents: List<CheckFEDataData.Tender.Document>) {
            val eligibleEvidences = tenderCriteria
                .asSequence()
                .flatMap { it.requirementGroups.asSequence() }
                .flatMap { it.requirements.asSequence() }
                .flatMap { it.eligibleEvidences?.asSequence() ?: emptySequence() }
                .toList()

            validateUniquenessBy(eligibleEvidences, "tender.criteria[].requirements[].eligibleEvidences[]") { it.id }
            checkEligibleEvidenceDocuments(eligibleEvidences, documents)
        }

        private fun checkEligibleEvidenceDocuments(
            eligibleEvidences: List<EligibleEvidence>,
            documents: List<CheckFEDataData.Tender.Document>
        ) {
            val eligibleEvidenceDocuments = eligibleEvidences
                .asSequence()
                .filter { it.type == EligibleEvidenceType.DOCUMENT }
                .mapNotNull { it.relatedDocument?.id }
                .toSet()

            val tenderDocuments = documents.toSet { it.id }

            if (!tenderDocuments.containsAll(eligibleEvidenceDocuments)) {
                throw ErrorException(
                    ErrorType.INVALID_ELIGIBLE_EVIDENCES,
                    message = "Tender documents do not contain all related documents of eligible evidences."
                )
            }
        }

        private fun checkExclusionClassification(
            tenderCriteria: List<CheckFEDataData.Tender.Criteria>,
            criteria: List<CheckFEDataData.Criterion>
        ) {
            val exclusionTenderCriteria = tenderCriteria.filter { criteria -> isOfExclusionCategory(criteria.classification.id) }
                .associateBy { it.classification.id }
            val exclusionCriteria = criteria.filter { criteria -> isOfExclusionCategory(criteria.classification.id) }
                .associateBy { it.classification.id }


            if (exclusionTenderCriteria.keys != exclusionCriteria.keys)
                throw ErrorException(
                    ErrorType.INVALID_CRITERIA,
                    message = "Exclusion criteria and tender.criteria does not match."
                )

            checkClassificationScheme(exclusionTenderCriteria, exclusionCriteria)
        }

        private fun checkSelectionClassification(
            tenderCriteria: List<CheckFEDataData.Tender.Criteria>,
            criteria: List<CheckFEDataData.Criterion>
        ) {
            val selectionTenderCriteria = tenderCriteria.filter { criteria -> isOfSelectionCategory(criteria.classification.id) }
                .associateBy { it.classification.id }
            val selectionCriteria = criteria.filter { criteria -> isOfSelectionCategory(criteria.classification.id) }
                .associateBy { it.classification.id }


            if (selectionTenderCriteria.keys != selectionCriteria.keys)
                throw ErrorException(
                    ErrorType.INVALID_CRITERIA,
                    message = "Selection criteria and tender.criteria does not match."
                )

            checkClassificationScheme(selectionTenderCriteria, selectionCriteria)
        }

        private fun checkClassificationScheme(
            tenderCriteria: Map<String, CheckFEDataData.Tender.Criteria>,
            criteria: Map<String, CheckFEDataData.Criterion>
        ) {
            tenderCriteria.forEach { (classificationId, tenderCriteria) ->
                val criteriaClassificationScheme = criteria.getValue(classificationId).classification.scheme
                val tenderCriteriaClassificationScheme = tenderCriteria.classification.scheme
                if (criteriaClassificationScheme != tenderCriteriaClassificationScheme)
                    throw ErrorException(
                        ErrorType.INVALID_CRITERIA,
                        message = "Scheme does not match for tender.criteria and criteria by classification id '$classificationId'"
                    )
            }
        }

        fun CheckFEDataData.Tender.SecondStage?.isNeedValidate(operationType: OperationType) =
            when(operationType) {
                OperationType.CREATE_FE -> true

                OperationType.AMEND_FE,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.AWARD_CONSIDERATION,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_AWARD,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_SUBMISSION,
                OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.DIVIDE_LOT,
                OperationType.ISSUING_FRAMEWORK_CONTRACT,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMISSION_PERIOD_END,
                OperationType.SUBMIT_BID,
                OperationType.TENDER_PERIOD_END,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_AWARD,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> false
            }

        fun List<CheckFEDataData.Tender.Criteria>.isNeedValidate(operationType: OperationType) =
            when(operationType) {
                OperationType.CREATE_FE -> true

                OperationType.AMEND_FE,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.AWARD_CONSIDERATION,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_SUBMISSION,
                OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.DIVIDE_LOT,
                OperationType.ISSUING_FRAMEWORK_CONTRACT,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMISSION_PERIOD_END,
                OperationType.SUBMIT_BID,
                OperationType.TENDER_PERIOD_END,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_AWARD,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_QUALIFICATION_PROTOCOL,
                OperationType.CREATE_AWARD-> false
            }

        fun CheckFEDataData.Tender.OtherCriteria?.isNeedValidate(operationType: OperationType) =
            when(operationType) {
                OperationType.CREATE_FE -> true

                OperationType.AMEND_FE,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.AWARD_CONSIDERATION,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_AWARD,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_SUBMISSION,
                OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.DIVIDE_LOT,
                OperationType.ISSUING_FRAMEWORK_CONTRACT,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMISSION_PERIOD_END,
                OperationType.SUBMIT_BID,
                OperationType.TENDER_PERIOD_END,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_AWARD,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> false
            }

        fun getEntity(tenderProcessDao: TenderProcessDao, context: CheckFEDataContext): TenderProcessEntity {
            val cpid = context.cpid
            val stage = when (context.operationType) {
                OperationType.AMEND_FE -> context.stage
                OperationType.CREATE_FE -> context.prevStage

                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.AWARD_CONSIDERATION,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_SUBMISSION,
                OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.DIVIDE_LOT,
                OperationType.ISSUING_FRAMEWORK_CONTRACT,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMISSION_PERIOD_END,
                OperationType.SUBMIT_BID,
                OperationType.TENDER_PERIOD_END,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_AWARD,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_QUALIFICATION_PROTOCOL,
                OperationType.CREATE_AWARD -> throw ErrorException(ErrorType.INVALID_PMD)
            }
            return tenderProcessDao.getByCpIdAndStage(cpId = cpid, stage = stage)
                ?: throw ErrorException(
                    error = ErrorType.ENTITY_NOT_FOUND,
                    message = "Cannot found tender (cpid='${cpid}' and stage='${stage}')"
                )
        }

        fun checkCriteriaValueScale(criteria: List<CheckFEDataData.Tender.Criteria>) {
            val allowedScale = 3

            criteria.asSequence()
                .flatMap { it.requirementGroups.asSequence() }
                .flatMap { it.requirements.asSequence() }
                .forEach { requirement ->
                    if (requirement.hasInvalidScale(allowedScale))
                        throw ErrorException(
                            error = ErrorType.INVALID_CRITERIA,
                            message = "Invalid scale ($allowedScale) for 'number' datatype"
                        )
                }
        }

        fun checkCriteriaReference(data: CheckFEDataData, context: CheckFEDataContext) {
            when (context.operationType) {
                OperationType.CREATE_FE ->
                    if (data.criteria.isEmpty())
                        throw ErrorException(ErrorType.MISSING_REFERENCE_CRITERIA, message = "VR-1.0.1.14.1")
                OperationType.AMEND_FE,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.AWARD_CONSIDERATION,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_AWARD,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_SUBMISSION,
                OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.DIVIDE_LOT,
                OperationType.ISSUING_FRAMEWORK_CONTRACT,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMISSION_PERIOD_END,
                OperationType.SUBMIT_BID,
                OperationType.TENDER_PERIOD_END,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_AWARD,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> Unit
            }
        }
    }
}