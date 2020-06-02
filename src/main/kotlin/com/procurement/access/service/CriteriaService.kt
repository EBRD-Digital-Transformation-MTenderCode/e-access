package com.procurement.access.service

import com.procurement.access.application.model.criteria.CreateCriteriaForProcuringEntity
import com.procurement.access.application.model.criteria.GetQualificationCriteriaAndMethod
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.success
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.dto.converter.create.convertToResponse
import com.procurement.access.infrastructure.dto.converter.get.criteria.convert
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.create.CreateCriteriaForProcuringEntityResult
import com.procurement.access.infrastructure.handler.get.criteria.GetQualificationCriteriaAndMethodResult
import com.procurement.access.utils.toJson
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface CriteriaService {
    fun getQualificationCriteriaAndMethod(params: GetQualificationCriteriaAndMethod.Params): Result<GetQualificationCriteriaAndMethodResult, Fail>
    fun createCriteriaForProcuringEntity(params: CreateCriteriaForProcuringEntity.Params): Result<CreateCriteriaForProcuringEntityResult, Fail>
}

@Service
class CriteriaServiceImpl(
    private val tenderProcessRepository: TenderProcessRepository
) : CriteriaService {

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
