package com.procurement.access.service

import com.procurement.access.application.model.context.CheckResponsesContext
import com.procurement.access.application.model.context.EvPanelsContext
import com.procurement.access.application.model.context.GetAwardCriteriaAndConversionsContext
import com.procurement.access.application.model.criteria.CreateCriteriaForProcuringEntity
import com.procurement.access.application.model.criteria.CriteriaId
import com.procurement.access.application.model.criteria.FindCriteria
import com.procurement.access.application.model.criteria.GetQualificationCriteriaAndMethod
import com.procurement.access.application.model.criteria.RequirementGroupId
import com.procurement.access.application.model.criteria.RequirementId
import com.procurement.access.application.model.data.GetAwardCriteriaAndConversionsResult
import com.procurement.access.application.model.data.RequestsForEvPanelsResult
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.CheckResponsesData
import com.procurement.access.application.service.tender.checkAnsweredOnce
import com.procurement.access.application.service.tender.checkAnsweredOnlyExpectedRequirement
import com.procurement.access.application.service.tender.checkDataTypeValue
import com.procurement.access.application.service.tender.checkIdsUniqueness
import com.procurement.access.application.service.tender.checkPeriod
import com.procurement.access.application.service.tender.checkResponsesCompleteness
import com.procurement.access.application.service.tender.checkResponsesRelationToOneGroup
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.EnumElementProvider.Companion.keysAsStrings
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.Result.Companion.success
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.domain.util.extension.mapResult
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.criteria.NoneValue
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.dto.converter.create.convertToResponse
import com.procurement.access.infrastructure.dto.converter.find.criteria.convert
import com.procurement.access.infrastructure.dto.converter.get.criteria.convert
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.handler.create.CreateCriteriaForProcuringEntityResult
import com.procurement.access.infrastructure.handler.find.criteria.FindCriteriaResult
import com.procurement.access.infrastructure.handler.get.criteria.GetQualificationCriteriaAndMethodResult
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface CriteriaService {
    fun checkResponses(context: CheckResponsesContext, data: CheckResponsesData)

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

        // FR.COM-1.16.12 & FR.COM-1.16.13
        checkResponsesCompleteness(criteria, data, Stage.creator(context.stage))

        // FR.COM-1.16.14
        checkResponsesRelationToOneGroup(criteria, data)

        // FR.COM-1.16.15
        checkAnsweredOnlyExpectedRequirement(criteria, data)

        // FR.COM-1.16.5
        checkDataTypeValue(data = data, criteria = criteria)
        // FR.COM-1.16.6
        checkAnsweredOnce(data = data)
        // FR.COM-1.16.7 & FR.COM-1.16.8
        checkPeriod(data = data)
        // FR.COM-1.16.9
        checkIdsUniqueness(data = data)
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
            source = CriteriaSource.PROCURING_ENTITY,
            relatesTo = CriteriaRelatesToEnum.AWARD,
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
                            description = null
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
                                        description = requirement.description
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
            .orForwardFail { error -> return error }
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
                    .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }

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
                    .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }

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
            .orForwardFail { fail -> return fail }

        return success(result)
    }

    override fun findCriteria(params: FindCriteria.Params): Result<FindCriteriaResult, Fail> {

        val entity = tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage)
            .orForwardFail { error -> return error }
            ?: return success(FindCriteriaResult(emptyList()))

        val foundedCriteriaResult = when (params.ocid.stage) {

            Stage.FE -> {
                val fe = entity.jsonData
                    .tryToObject(FEEntity::class.java)
                    .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }

                val targetCriteria = fe.tender.criteria.orEmpty()
                    .asSequence()
                    .filter { it.source == params.source }
                    .map { criterion -> criterion.convert() }
                    .toList()

                success(targetCriteria)
            }

            Stage.EV,
            Stage.NP,
            Stage.TP -> {
                val cn = entity.jsonData
                    .tryToObject(CNEntity::class.java)
                    .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }

                val targetCriteria = cn.tender.criteria.orEmpty()
                    .asSequence()
                    .filter { it.source == params.source }
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
            .orForwardFail { fail -> return fail }

        val result = FindCriteriaResult(foundedCriteriaResult)

        return success(result)
    }

    override fun createCriteriaForProcuringEntity(params: CreateCriteriaForProcuringEntity.Params): Result<CreateCriteriaForProcuringEntityResult, Fail> {
        val stage = params.ocid.stage

        val tenderProcessEntity = tenderProcessRepository.getByCpIdAndStage(
            cpid = params.cpid,
            stage = stage
        )
            .orForwardFail { error -> return error }
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
                    .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }

                val createdCriteria = params.criteria
                    .map { criterion -> createCriterionForCN(criterion, params.operationType) }

                val result = createdCriteria.map { it.convertToResponse() }

                val updatedCnEntity = cn.copy(
                    tender = cn.tender.copy(
                        criteria = (cn.tender.criteria ?: emptyList()) + createdCriteria
                    )
                )
                val updatedTenderProcessEntity = tenderProcessEntity.copy(jsonData = toJson(updatedCnEntity))

                tenderProcessRepository.save(updatedTenderProcessEntity)
                    .orForwardFail { incident -> return incident }

                success(result)
            }

            Stage.FE -> {
                val cn = tenderProcessEntity.jsonData
                    .tryToObject(FEEntity::class.java)
                    .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }

                val createdCriteria = params.criteria
                    .mapResult { criterion -> createCriterionForFE(criterion, params.operationType) }
                    .orForwardFail { error -> return error }

                val result = createdCriteria.map { it.convertToResponse() }

                val updatedFeEntity = cn.copy(
                    tender = cn.tender.copy(
                        criteria = (cn.tender.criteria ?: emptyList()) + createdCriteria
                    )
                )
                val updatedTenderProcessEntity = tenderProcessEntity.copy(jsonData = toJson(updatedFeEntity))

                tenderProcessRepository.save(updatedTenderProcessEntity)
                    .orForwardFail { incident -> return incident }

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
            .orForwardFail { error -> return error }

        return success(CreateCriteriaForProcuringEntityResult(result))
    }

    private fun createCriterionForCN(
        criterion: CreateCriteriaForProcuringEntity.Params.Criterion,
        operationType: OperationType
    ): CNEntity.Tender.Criteria =
        CNEntity.Tender.Criteria(
            id = criterion.id,
            title = criterion.title,
            description = criterion.description,
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
                                    dataType = RequirementDataType.BOOLEAN // FR.COM-1.12.2
                                )
                            }
                    )
                },
            source = CriteriaSource.PROCURING_ENTITY, // FR.COM-1.12.1
            relatesTo = when (operationType) {
                OperationType.AMEND_FE,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.COMPLETE_QUALIFICATION,
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
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> null

                OperationType.SUBMISSION_PERIOD_END -> CriteriaRelatesToEnum.QUALIFICATION
                OperationType.TENDER_PERIOD_END -> CriteriaRelatesToEnum.AWARD
            },
            relatedItem = null
        )

    private fun createCriterionForFE(
        criterion: CreateCriteriaForProcuringEntity.Params.Criterion,
        operationType: OperationType
    ): Result<FEEntity.Tender.Criteria, DataErrors.Validation.UnknownValue>  {

        return FEEntity.Tender.Criteria(
            id = criterion.id,
            title = criterion.title,
            description = criterion.description,
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
                                    dataType = RequirementDataType.BOOLEAN // FR.COM-1.12.2
                                )
                            }
                    )
                },
            source = CriteriaSource.PROCURING_ENTITY, // FR.COM-1.12.1
            relatesTo = when (operationType) {
                OperationType.AMEND_FE,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.COMPLETE_QUALIFICATION,
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
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.UPDATE_AP,
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

                OperationType.SUBMISSION_PERIOD_END -> CriteriaRelatesToEnum.QUALIFICATION
                OperationType.TENDER_PERIOD_END -> CriteriaRelatesToEnum.AWARD
            }
        )
            .asSuccess()
    }

}
