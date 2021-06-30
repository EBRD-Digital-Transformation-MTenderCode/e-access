package com.procurement.access.service

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
import com.procurement.access.application.model.data.fromDomain
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.EnumElementProvider.Companion.keysAsStrings
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.fail.error.ValidationErrors
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
import com.procurement.access.infrastructure.entity.RfqEntity
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
    override fun getCriteriaForTenderer(context: GetCriteriaForTendererContext): GetCriteriaForTendererResult {
        val entity = tenderProcessRepository.getByCpIdAndOcid(cpid = context.cpid, ocid = context.ocid)
            .orThrow { it.exception }
            ?: throw ErrorException(
                error = ErrorType.DATA_NOT_FOUND,
                message = "VR.COM-1.42.1"
            )

        val criteriaForTenderer = when (context.ocid.stage) {
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

            Stage.RQ -> emptyList()

            Stage.AC,
            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.NP,
            Stage.PC,
            Stage.PN,
            Stage.PO -> throw ErrorException(
                error = ErrorType.INVALID_STAGE,
                message = "Stage ${context.ocid.stage} not allowed at the command."
            )
        }

        val criteriaWithActiveRequirements = criteriaForTenderer
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

        return GetCriteriaForTendererResult(criteriaWithActiveRequirements)

    }

    override fun createRequestsForEvPanels(context: EvPanelsContext): RequestsForEvPanelsResult {
        val entity: TenderProcessEntity = tenderProcessDao.getByCpidAndOcid(cpid = context.cpid, ocid = context.ocid)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)

        val result = when (context.ocid.stage) {
            Stage.AC,
            Stage.EV,
            Stage.FE,
            Stage.NP,
            Stage.TP -> {
                val cn = toObject(CNEntity::class.java, entity.jsonData)
                val tender = cn.tender
                val storedCriteria = tender.criteria.orEmpty()
                val criterionForEvPanels = createCriterionForEvPanels(context.startDate)

                val updatedTender = tender.copy(criteria =  storedCriteria + listOf(criterionForEvPanels))
                val updatedCNEntity = cn.copy(tender = updatedTender)

                tenderProcessDao.save(entity.copy(jsonData = toJson(updatedCNEntity)))

                RequestsForEvPanelsResult.Criteria.fromDomain(criterionForEvPanels)
                    .let { RequestsForEvPanelsResult(it) }
            }

            Stage.RQ -> {
                val criterionForEvPanels = createCriterionForEvPanels(context.startDate)
                RequestsForEvPanelsResult.Criteria.fromDomain(criterionForEvPanels)
                    .let { RequestsForEvPanelsResult(it) }
            }

            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PN,
            Stage.PO -> throw ErrorException(
                error = ErrorType.INVALID_STAGE,
                message = "Stage ${context.ocid.stage} not allowed at the command."
            )
        }

        return result
    }

    fun createCriterionForEvPanels(datePublished: LocalDateTime) =
        CNEntity.Tender.Criteria(
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
                            datePublished = datePublished
                        )
                    )
                )
            )
        )

    override fun getAwardCriteriaAndConversions(context: GetAwardCriteriaAndConversionsContext): GetAwardCriteriaAndConversionsResult? {
        val tenderEntity = tenderProcessDao.getByCpidAndOcid(cpid = context.cpid, ocid = context.ocid)

        val result = when (context.ocid.stage) {
            Stage.AC,
            Stage.EV,
            Stage.FE,
            Stage.NP,
            Stage.TP ->
                tenderEntity?.let { entity ->
                    val cn = toObject(CNEntity::class.java, tenderEntity.jsonData)
                    GetAwardCriteriaAndConversionsResult.fromDomain(cn)
                }

            Stage.RQ ->
                tenderEntity?.let { entity ->
                    val rq = toObject(RfqEntity::class.java, entity.jsonData)
                    GetAwardCriteriaAndConversionsResult.fromDomain(rq)
                }

            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PN,
            Stage.PO -> throw ErrorException(
                error = ErrorType.INVALID_STAGE,
                message = "Stage ${context.ocid.stage} not allowed at the command."
            )
        }

        return result
    }

    override fun getQualificationCriteriaAndMethod(params: GetQualificationCriteriaAndMethod.Params): Result<GetQualificationCriteriaAndMethodResult, Fail> {
        val entity = tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid)
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
            Stage.PN,
            Stage.PO,
            Stage.RQ ->
                failure(
                    ValidationErrors.UnexpectedStageForGetQualificationCriteriaAndMethod(stage = params.ocid.stage)
                )
        }
            .onFailure { fail -> return fail }

        return success(result)
    }

    override fun findCriteria(params: FindCriteria.Params): Result<FindCriteriaResult, Fail> {

        val entity = tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid)
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
            Stage.PN,
            Stage.PO,
            Stage.RQ ->
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

        val tenderProcessEntity = tenderProcessRepository.getByCpIdAndOcid(
            cpid = params.cpid,
            ocid = params.ocid
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

            Stage.AC,
            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PN,
            Stage.PO,
            Stage.RQ ->
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
                OperationType.APPLY_CONFIRMATIONS,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.AWARD_CONSIDERATION,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_AWARD,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_CONTRACT,
                OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
                OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE,
                OperationType.CREATE_FE,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_RFQ,
                OperationType.CREATE_SUBMISSION,
                OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.DIVIDE_LOT,
                OperationType.ISSUING_FRAMEWORK_CONTRACT,
                OperationType.NEXT_STEP_AFTER_BUYERS_CONFIRMATION,
                OperationType.NEXT_STEP_AFTER_INVITED_CANDIDATES_CONFIRMATION,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMIT_BID,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_AWARD,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_BID,
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
                OperationType.APPLY_CONFIRMATIONS,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.AWARD_CONSIDERATION,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_AWARD,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_CONTRACT,
                OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
                OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE,
                OperationType.CREATE_FE,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_RFQ,
                OperationType.CREATE_SUBMISSION,
                OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.DIVIDE_LOT,
                OperationType.ISSUING_FRAMEWORK_CONTRACT,
                OperationType.NEXT_STEP_AFTER_BUYERS_CONFIRMATION,
                OperationType.NEXT_STEP_AFTER_INVITED_CANDIDATES_CONFIRMATION,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.RELATION_AP,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMIT_BID,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_AWARD,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_BID,
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
