package com.procurement.access.service

import com.procurement.access.application.model.criteria.CreateCriteriaForProcuringEntity
import com.procurement.access.application.model.context.CheckResponsesContext
import com.procurement.access.application.model.context.EvPanelsContext
import com.procurement.access.application.model.context.GetAwardCriteriaAndConversionsContext
import com.procurement.access.application.model.criteria.CriteriaId
import com.procurement.access.application.model.criteria.GetQualificationCriteriaAndMethod
import com.procurement.access.application.model.criteria.RequirementGroupId
import com.procurement.access.application.model.criteria.RequirementId
import com.procurement.access.application.model.data.GetAwardCriteriaAndConversionsResult
import com.procurement.access.application.model.data.RequestsForEvPanelsResult
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.CheckResponsesData
import com.procurement.access.application.service.tender.checkAnswerCompleteness
import com.procurement.access.application.service.tender.checkAnsweredOnce
import com.procurement.access.application.service.tender.checkDataTypeValue
import com.procurement.access.application.service.tender.checkIdsUniqueness
import com.procurement.access.application.service.tender.checkPeriod
import com.procurement.access.application.service.tender.checkRequirementRelationRelevance
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.success
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.dto.converter.create.convertToResponse
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.criteria.NoneValue
import com.procurement.access.infrastructure.dto.converter.get.criteria.convert
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.create.CreateCriteriaForProcuringEntityResult
import com.procurement.access.infrastructure.handler.get.criteria.GetQualificationCriteriaAndMethodResult
import com.procurement.access.utils.toJson
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toObject
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface CriteriaService {
    fun checkResponses(context: CheckResponsesContext, data: CheckResponsesData)

    fun createRequestsForEvPanels(context: EvPanelsContext): RequestsForEvPanelsResult

    fun getAwardCriteriaAndConversions(context: GetAwardCriteriaAndConversionsContext): GetAwardCriteriaAndConversionsResult?

    fun getQualificationCriteriaAndMethod(params: GetQualificationCriteriaAndMethod.Params): Result<GetQualificationCriteriaAndMethodResult, Fail>
    fun createCriteriaForProcuringEntity(params: CreateCriteriaForProcuringEntity.Params): Result<CreateCriteriaForProcuringEntityResult, Fail>
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

        //FR.COM-1.16.1
        val criteria = cnEntity.tender.criteria
            ?: if (data.bid.requirementResponses.isEmpty())
                return
            else
                throw ErrorException(
                    error = ErrorType.ENTITY_NOT_FOUND,
                    message = "Bid.RequirementResponse object is present in request but no record found in DB."
                )

        val requirementResponses = data.bid.requirementResponses
        if (criteria.isNotEmpty() && requirementResponses.isEmpty())
            throw ErrorException(
                error = ErrorType.INVALID_REQUIREMENT_RESPONSE,
                message = "No requirement responses found for requirement from criteria."
            )

        // FReq-1.2.1.1
        checkRequirementRelationRelevance(data = data, criteria = criteria)
        // FReq-1.2.1.2
        checkAnswerCompleteness(data = data, criteria = criteria)
        // FReq-1.2.1.3
        checkAnsweredOnce(data = data)
        // FReq-1.2.1.4
        checkDataTypeValue(data = data, criteria = criteria)
        // FReq-1.2.1.5 & FReq-1.2.1.7
        checkPeriod(data = data)
        // FReq-1.2.1.6
        checkIdsUniqueness(data = data, criteria = criteria)
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
            ?: return Result.failure(
                ValidationErrors.TenderNotFoundOnGetQualificationCriteriaAndMethod(
                    cpid = params.cpid,
                    ocid = params.ocid
                )
            )

        val cnEntity = entity.jsonData
            .tryToObject(CNEntity::class.java)
            .doReturn { error ->
                return Result.failure(Fail.Incident.DatabaseIncident(exception = error.exception))
            }

        val tender = cnEntity.tender
        val otherCriteria = tender.otherCriteria!!

        val result = convert(
            conversions = tender.conversions.orEmpty(),
            qualificationSystemMethods = otherCriteria.qualificationSystemMethods,
            reductionCriteria = otherCriteria.reductionCriteria
        )

        return Result.success(result)
    }

    override fun createCriteriaForProcuringEntity(params: CreateCriteriaForProcuringEntity.Params): Result<CreateCriteriaForProcuringEntityResult, Fail> {

        val tenderProcessEntity = tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage)
            .orForwardFail { error -> return error }
            ?: return Result.failure(
                ValidationErrors.TenderNotFoundOnCreateCriteriaForProcuringEntity(
                    cpid = params.cpid,
                    ocid = params.ocid
                )
            )

        val cnEntity = tenderProcessEntity.jsonData
            .tryToObject(CNEntity::class.java)
            .doReturn { error ->
                return Result.failure(Fail.Incident.DatabaseIncident(exception = error.exception))
            }

        val createdCriteria = params.criteria
            .map { criterion ->
                CNEntity.Tender.Criteria(
                    id                = criterion.id,
                    title             = criterion.title,
                    description       = criterion.description,
                    requirementGroups = criterion.requirementGroups
                        .map { requirementGroups ->
                            CNEntity.Tender.Criteria.RequirementGroup(
                                id           = requirementGroups.id,
                                description  = requirementGroups.description,
                                requirements = requirementGroups.requirements
                                    .map { requirement ->
                                        Requirement(
                                            id          = requirement.id,
                                            description = requirement.description,
                                            title       = requirement.title,
                                            period      = null,
                                            value       = null,
                                            dataType    = RequirementDataType.BOOLEAN // FR.COM-1.12.2
                                        )
                                    }
                            )
                        },
                    source      = CriteriaSource.PROCURING_ENTITY, // FR.COM-1.12.1
                    relatedItem = null,
                    relatesTo   = null
                )
            }

        val result = createdCriteria.map { it.convertToResponse() }

        val updatedCnEntity = cnEntity.copy(
            tender = cnEntity.tender.copy(
                criteria = createdCriteria
            )
        )
        val updatedTenderProcessEntity = tenderProcessEntity.copy(jsonData = toJson(updatedCnEntity))

        tenderProcessRepository.save(updatedTenderProcessEntity)
            .orForwardFail { incident -> return incident }

        return success(CreateCriteriaForProcuringEntityResult(result))
    }
}
