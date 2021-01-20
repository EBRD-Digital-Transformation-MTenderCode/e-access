package com.procurement.access.service

import com.procurement.access.application.model.context.CheckResponsesContext
import com.procurement.access.application.model.context.EvPanelsContext
import com.procurement.access.application.model.context.GetAwardCriteriaAndConversionsContext
import com.procurement.access.application.model.context.GetCriteriaForTendererContext
import com.procurement.access.application.model.criteria.CreateCriteriaForProcuringEntity
import com.procurement.access.application.model.criteria.CriteriaId
import com.procurement.access.application.model.criteria.FindCriteria
import com.procurement.access.application.model.criteria.GetQualificationCriteriaAndMethod
import com.procurement.access.application.model.criteria.RequirementGroupId
import com.procurement.access.application.model.criteria.RequirementId
import com.procurement.access.application.model.data.GetAwardCriteriaAndConversionsResult
import com.procurement.access.application.model.data.GetCriteriaForTendererResult
import com.procurement.access.application.model.data.RequestsForEvPanelsResult
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.CheckResponsesData
import com.procurement.access.application.service.tender.checkAnsweredOnce
import com.procurement.access.application.service.tender.checkDataTypeValue
import com.procurement.access.application.service.tender.checkIdsUniqueness
import com.procurement.access.application.service.tender.checkPeriod
import com.procurement.access.application.service.tender.checkResponsesCompleteness
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.EnumElementProvider.Companion.keysAsStrings
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.enums.RequirementStatus
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.requirement.NoneValue
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v1.converter.convertToResponse
import com.procurement.access.infrastructure.handler.v2.model.response.CreateCriteriaForProcuringEntityResult
import com.procurement.access.infrastructure.handler.v2.model.response.FindCriteriaResult
import com.procurement.access.infrastructure.handler.v2.model.response.GetQualificationCriteriaAndMethodResult
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import com.procurement.access.lib.functional.Result.Companion.success
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface CriteriaService {
    fun checkResponses(context: CheckResponsesContext, data: CheckResponsesData)

    fun getCriteriaForTenderer(context: GetCriteriaForTendererContext): GetCriteriaForTendererResult

    fun createRequestsForEvPanels(context: EvPanelsContext): RequestsForEvPanelsResult

    fun getAwardCriteriaAndConversions(context: GetAwardCriteriaAndConversionsContext): GetAwardCriteriaAndConversionsResult?

    fun getQualificationCriteriaAndMethod(params: GetQualificationCriteriaAndMethod.Params): Result<GetQualificationCriteriaAndMethodResult, Fail>

    fun createCriteriaForProcuringEntity(params: CreateCriteriaForProcuringEntity.Params): Result<CreateCriteriaForProcuringEntityResult, Fail>

    fun findCriteria(params: FindCriteria.Params): Result<FindCriteriaResult, Fail>
}

@Service
class CriteriaServiceImpl(
    private val tenderProcessRepository: TenderProcessRepository,
    private val tenderProcessDao: TenderProcessDao
) : CriteriaService {

    override fun checkResponses(context: CheckResponsesContext, data: CheckResponsesData) {

        val entity = tenderProcessDao.getByCpIdAndStage(cpId = context.cpid, stage = context.stage)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val cnEntity = toObject(CNEntity::class.java, entity.jsonData)
        val criteria = cnEntity.tender.criteria.orEmpty()

        // FR.COM-1.16.12 & FR.COM-1.16.13 && FR.COM-1.16.15 && FR.COM-1.16.14
        checkResponsesCompleteness(criteria, data, Stage.creator(context.stage))

        // FR.COM-1.16.5
        checkDataTypeValue(data = data, criteria = criteria)
        // FR.COM-1.16.6
        checkAnsweredOnce(data = data)
        // FR.COM-1.16.7 & FR.COM-1.16.8
        checkPeriod(data = data)
        // FR.COM-1.16.9
        checkIdsUniqueness(data = data)
    }

    override fun getCriteriaForTenderer(context: GetCriteriaForTendererContext): GetCriteriaForTendererResult {
        val validatedCpid = Cpid.tryCreate(context.cpid)
            .orThrow { _ ->
                ErrorException(
                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                    message = "Attribute 'cpid' has invalid format."
                )
            }

        val validatedStage = Stage.tryOf(context.stage)
            .orThrow { _ ->
                ErrorException(
                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                    message = "Attribute 'stage' has invalid value."
                )
            }

        val entity = tenderProcessRepository.getByCpIdAndStage(cpid = validatedCpid, stage = validatedStage)
            .orThrow { it.exception }
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)

        val criteriaForTenderer = when (validatedStage) {
            Stage.EV,
            Stage.TP -> {
                toObject(CNEntity::class.java, entity.jsonData)
                    .tender.criteria
                    ?.filter { it.source == CriteriaSource.TENDERER }
                    .orEmpty()
                    .map { criterion -> GetCriteriaForTendererResult.fromDomain(criterion) }
            }

            Stage.FE -> {
                toObject(FEEntity::class.java, entity.jsonData)
                    .tender.criteria
                    ?.filter { it.source == CriteriaSource.TENDERER }
                    .orEmpty()
                    .map { criterion -> GetCriteriaForTendererResult.fromDomain(criterion) }
            }

            Stage.AC,
            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.NP,
            Stage.PC,
            Stage.PN -> throw ErrorException(
                error = ErrorType.INVALID_STAGE,
                message = "Stage $validatedStage not allowed at the command."
            )
        }

        return GetCriteriaForTendererResult(criteriaForTenderer)

    }

    override fun createRequestsForEvPanels(context: EvPanelsContext): RequestsForEvPanelsResult {
        val entity: TenderProcessEntity = tenderProcessDao.getByCpIdAndStage(cpId = context.cpid, stage = context.stage)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val cnEntity = toObject(CNEntity::class.java, entity.jsonData)
        val tender = cnEntity.tender
        val criteria = tender.criteria.orEmpty()

        val criterion = CNEntity.Tender.Criteria(
            id = CriteriaId.Permanent.generate().toString(),
            title = "",
            description = "",
            classification = CNEntity.Tender.Criteria.Classification(
                id = "CRITERION.EXCLUSION.CONFLICT_OF_INTEREST.TBD",
                scheme = "ESPD"
            ),
            source = CriteriaSource.PROCURING_ENTITY,
            relatesTo = CriteriaRelatesTo.AWARD,
            relatedItem = null,
            requirementGroups = listOf(
                CNEntity.Tender.Criteria.RequirementGroup(
                    id = RequirementGroupId.Permanent.generate().toString(),
                    description = null,
                    requirements = listOf(
                        Requirement(
                            id = RequirementId.Permanent.generate().toString(),
                            title = "",
                            dataType = RequirementDataType.BOOLEAN,
                            value = NoneValue,
                            period = null,
                            description = null,
                            eligibleEvidences = emptyList(),
                            status = RequirementStatus.ACTIVE,
                            datePublished = context.startDate
                        )
                    )
                )
            )
        )

        val updatedCriteria = criteria + listOf(criterion)
        val updatedTender = tender.copy(
            criteria = updatedCriteria
        )
        val updatedCNEntity = cnEntity.copy(
            tender = updatedTender
        )

        tenderProcessDao.save(
            entity.copy(
                jsonData = toJson(updatedCNEntity)
            )
        )

        return RequestsForEvPanelsResult(
            criteria = RequestsForEvPanelsResult.Criteria(
                id = criterion.id,
                title = criterion.title,
                description = criterion.description,
                source = criterion.source!!,
                relatesTo = criterion.relatesTo!!,
                classification = criterion.classification
                    .let { classification ->
                        RequestsForEvPanelsResult.Criteria.Classification(
                            id = classification.id,
                            scheme = classification.scheme
                        )
                    },
                requirementGroups = criterion.requirementGroups
                    .map { requirementGroup ->
                        RequestsForEvPanelsResult.Criteria.RequirementGroup(
                            id = requirementGroup.id,
                            requirements = requirementGroup.requirements
                                .map { requirement ->
                                    Requirement(
                                        id = requirement.id,
                                        title = requirement.title,
                                        dataType = requirement.dataType,
                                        value = requirement.value,
                                        period = requirement.period,
                                        description = requirement.description,
                                        eligibleEvidences = requirement.eligibleEvidences?.toList(),
                                        status = requirement.status,
                                        datePublished = requirement.datePublished
                                    )
                                }
                        )
                    }
            )
        )
    }

    override fun getAwardCriteriaAndConversions(context: GetAwardCriteriaAndConversionsContext): GetAwardCriteriaAndConversionsResult? =
        tenderProcessDao.getByCpIdAndStage(cpId = context.cpid, stage = context.stage)
            ?.let { entity ->
                val cn = toObject(CNEntity::class.java, entity.jsonData)
                val conversions = cn.tender.conversions
                    ?.map { conversion ->
                        GetAwardCriteriaAndConversionsResult.Conversion(
                            id = conversion.id,
                            relatesTo = conversion.relatesTo,
                            relatedItem = conversion.relatedItem,
                            description = conversion.description,
                            rationale = conversion.rationale,
                            coefficients = conversion.coefficients
                                .map { coefficient ->
                                    GetAwardCriteriaAndConversionsResult.Conversion.Coefficient(
                                        id = coefficient.id,
                                        value = coefficient.value,
                                        coefficient = coefficient.coefficient
                                    )
                                }
                        )
                    }

                GetAwardCriteriaAndConversionsResult(
                    awardCriteria = cn.tender.awardCriteria!!,
                    awardCriteriaDetails = cn.tender.awardCriteriaDetails!!,
                    conversions = conversions
                )
            }

    override fun getQualificationCriteriaAndMethod(params: GetQualificationCriteriaAndMethod.Params): Result<GetQualificationCriteriaAndMethodResult, Fail> {
        val stage = params.ocid.stage

        val entity = tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = stage)
            .onFailure { error -> return error }
            ?: return failure(
                ValidationErrors.TenderNotFoundOnGetQualificationCriteriaAndMethod(
                    cpid = params.cpid,
                    ocid = params.ocid
                )
            )

        val result = when (params.ocid.stage) {
            Stage.FE -> {
                val fe = entity.jsonData
                    .tryToObject(FEEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val tender = fe.tender
                val otherCriteria = tender.otherCriteria!!

                val result = convert(
                    conversions = emptyList(),
                    qualificationSystemMethods = otherCriteria.qualificationSystemMethods,
                    reductionCriteria = otherCriteria.reductionCriteria
                )

                success(result)
            }

            Stage.EV,
            Stage.NP,
            Stage.TP -> {
                val cn = entity.jsonData
                    .tryToObject(CNEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val tender = cn.tender
                val otherCriteria = tender.otherCriteria!!

                val result = convert(
                    conversions = tender.conversions.orEmpty(),
                    qualificationSystemMethods = otherCriteria.qualificationSystemMethods,
                    reductionCriteria = otherCriteria.reductionCriteria
                )

                success(result)
            }

            Stage.AC,
            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PN ->
                failure(
                    ValidationErrors.UnexpectedStageForGetQualificationCriteriaAndMethod(stage = params.ocid.stage)
                )
        }
            .onFailure { fail -> return fail }

        return success(result)
    }

    override fun findCriteria(params: FindCriteria.Params): Result<FindCriteriaResult, Fail> {

        val entity = tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage)
            .onFailure { error -> return error }
            ?: return success(FindCriteriaResult(emptyList()))

        val allFoundedCriteriaBySource = when (params.ocid.stage) {

            Stage.FE -> {
                val fe = entity.jsonData
                    .tryToObject(FEEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val targetCriteria = fe.tender.criteria.orEmpty()
                    .asSequence()
                    .filter { it.source in params.source }
                    .map { criterion -> criterion.convert() }
                    .toList()

                success(targetCriteria)
            }

            Stage.EV,
            Stage.NP,
            Stage.TP -> {
                val cn = entity.jsonData
                    .tryToObject(CNEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val targetCriteria = cn.tender.criteria.orEmpty()
                    .asSequence()
                    .filter { it.source in params.source }
                    .map { criterion -> criterion.convert() }
                    .toList()

                success(targetCriteria)
            }

            Stage.AC,
            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PN ->
                failure(
                    ValidationErrors.UnexpectedStageForFindCriteria(stage = params.ocid.stage)
                )
        }
            .onFailure { fail -> return fail }

        val criteriaWithActiveRequirements = allFoundedCriteriaBySource
            .map {
                it.copy(
                    requirementGroups = it.requirementGroups
                        .map {
                            it.copy(requirements = it.requirements.filter { it.status == RequirementStatus.ACTIVE })
                        }
                        .filter { it.requirements.isNotEmpty() }
                )
            }
            .filter { it.requirementGroups.isNotEmpty() }


        val result = FindCriteriaResult(criteriaWithActiveRequirements)

        return success(result)
    }

    override fun createCriteriaForProcuringEntity(params: CreateCriteriaForProcuringEntity.Params): Result<CreateCriteriaForProcuringEntityResult, Fail> {
        val stage = params.ocid.stage
        val datePublished = params.date

        val tenderProcessEntity = tenderProcessRepository.getByCpIdAndStage(
            cpid = params.cpid,
            stage = stage
        )
            .onFailure { error -> return error }
            ?: return failure(
                ValidationErrors.TenderNotFoundOnCreateCriteriaForProcuringEntity(
                    cpid = params.cpid,
                    ocid = params.ocid
                )
            )

        val result = when (stage) {
            Stage.EV,
            Stage.NP,
            Stage.TP -> {
                val cn = tenderProcessEntity.jsonData
                    .tryToObject(CNEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val createdCriteria = params.criteria
                    .map { criterion -> createCriterionForCN(datePublished, criterion, params.operationType) }

                val result = createdCriteria.map { it.convertToResponse() }

                val updatedCnEntity = cn.copy(
                    tender = cn.tender.copy(
                        criteria = (cn.tender.criteria ?: emptyList()) + createdCriteria
                    )
                )
                val updatedTenderProcessEntity = tenderProcessEntity.copy(jsonData = toJson(updatedCnEntity))

                tenderProcessRepository.save(updatedTenderProcessEntity)
                    .onFailure { incident -> return incident }

                success(result)
            }

            Stage.FE -> {
                val cn = tenderProcessEntity.jsonData
                    .tryToObject(FEEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val createdCriteria = params.criteria
                    .mapResult { criterion -> createCriterionForFE(datePublished, criterion, params.operationType) }
                    .onFailure { error -> return error }

                val result = createdCriteria.map { it.convertToResponse() }

                val updatedFeEntity = cn.copy(
                    tender = cn.tender.copy(
                        criteria = (cn.tender.criteria ?: emptyList()) + createdCriteria
                    )
                )
                val updatedTenderProcessEntity = tenderProcessEntity.copy(jsonData = toJson(updatedFeEntity))

                tenderProcessRepository.save(updatedTenderProcessEntity)
                    .onFailure { incident -> return incident }

                success(result)
            }

            Stage.AP,
            Stage.AC,
            Stage.EI,
            Stage.PC,
            Stage.PN,
            Stage.FS ->
                failure(
                    ValidationErrors.UnexpectedStageForCreateCriteriaForProcuringEntity(stage = params.ocid.stage)
                )
        }
            .onFailure { error -> return error }

        return success(CreateCriteriaForProcuringEntityResult(result))
    }

    private fun createCriterionForCN(
        datePublished: LocalDateTime,
        criterion: CreateCriteriaForProcuringEntity.Params.Criterion,
        operationType: OperationType
    ): CNEntity.Tender.Criteria =
        CNEntity.Tender.Criteria(
            id = criterion.id,
            title = criterion.title,
            description = criterion.description,
            classification = criterion.classification
                .let { classification ->
                    CNEntity.Tender.Criteria.Classification(
                        id = classification.id,
                        scheme = classification.scheme
                    )
                },
            requirementGroups = criterion.requirementGroups
                .map { requirementGroups ->
                    CNEntity.Tender.Criteria.RequirementGroup(
                        id = requirementGroups.id,
                        description = requirementGroups.description,
                        requirements = requirementGroups.requirements
                            .map { requirement ->
                                Requirement(
                                    id = requirement.id,
                                    description = requirement.description,
                                    title = requirement.title,
                                    period = null,
                                    value = NoneValue,
                                    dataType = RequirementDataType.BOOLEAN, // FR.COM-1.12.2
                                    eligibleEvidences = emptyList(),
                                    status = RequirementStatus.ACTIVE,
                                    datePublished = datePublished
                                )
                            }
                    )
                },
            source = CriteriaSource.PROCURING_ENTITY, // FR.COM-1.12.1
            relatesTo = when (operationType) {
                OperationType.AMEND_FE,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_AWARD,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_FE,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_SUBMISSION,
                OperationType.DIVIDE_LOT,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMIT_BID,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_AWARD,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> null

                OperationType.SUBMISSION_PERIOD_END -> CriteriaRelatesTo.QUALIFICATION
                OperationType.TENDER_PERIOD_END -> CriteriaRelatesTo.AWARD
            },
            relatedItem = null
        )

    private fun createCriterionForFE(
        datePublished: LocalDateTime,
        criterion: CreateCriteriaForProcuringEntity.Params.Criterion,
        operationType: OperationType
    ): Result<FEEntity.Tender.Criteria, DataErrors.Validation.UnknownValue>  {

        return FEEntity.Tender.Criteria(
            id = criterion.id,
            title = criterion.title,
            description = criterion.description,
            classification = criterion.classification
                .let { classification ->
                    FEEntity.Tender.Criteria.Classification(
                        id = classification.id,
                        scheme = classification.scheme
                    )
                },
            requirementGroups = criterion.requirementGroups
                .map { requirementGroups ->
                    FEEntity.Tender.Criteria.RequirementGroup(
                        id = requirementGroups.id,
                        description = requirementGroups.description,
                        requirements = requirementGroups.requirements
                            .map { requirement ->
                                Requirement(
                                    id = requirement.id,
                                    description = requirement.description,
                                    title = requirement.title,
                                    period = null,
                                    value = NoneValue,
                                    dataType = RequirementDataType.BOOLEAN, // FR.COM-1.12.2
                                    eligibleEvidences = emptyList(),
                                    status = RequirementStatus.ACTIVE,
                                    datePublished = datePublished
                                )
                            }
                    )
                },
            source = CriteriaSource.PROCURING_ENTITY, // FR.COM-1.12.1
            relatesTo = when (operationType) {
                OperationType.AMEND_FE,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_AWARD,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_FE,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_SUBMISSION,
                OperationType.DIVIDE_LOT,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMIT_BID,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_AWARD,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_QUALIFICATION_PROTOCOL ->
                    return failure(
                        DataErrors.Validation.UnknownValue(
                            name = "operationType",
                            expectedValues = CreateCriteriaForProcuringEntity.Params.allowedOperationType.keysAsStrings(),
                            actualValue = operationType.toString()
                        )
                    )

                OperationType.SUBMISSION_PERIOD_END -> CriteriaRelatesTo.QUALIFICATION
                OperationType.TENDER_PERIOD_END -> CriteriaRelatesTo.AWARD
            }
        )
            .asSuccess()
    }

}
