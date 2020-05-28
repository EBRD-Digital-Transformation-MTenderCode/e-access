package com.procurement.access.service

import com.procurement.access.application.model.criteria.GetQualificationCriteriaAndMethod
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.dto.converter.get.criteria.convert
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.get.criteria.GetQualificationCriteriaAndMethodResult
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface CriteriaService {
    fun getQualificationCriteriaAndMethod(params: GetQualificationCriteriaAndMethod.Params): Result<GetQualificationCriteriaAndMethodResult, Fail>
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
}
