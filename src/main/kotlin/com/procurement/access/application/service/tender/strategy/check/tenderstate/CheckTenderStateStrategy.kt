package com.procurement.access.application.service.tender.strategy.check.tenderstate

import com.procurement.access.application.model.params.CheckTenderStateParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.Fail.Incident.DatabaseIncident
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.rule.TenderStatesRule
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.domain.util.asValidationFailure
import com.procurement.access.infrastructure.entity.TenderStateInfo
import com.procurement.access.service.RulesService
import com.procurement.access.utils.tryToObject

class CheckTenderStateStrategy(
    private val tenderProcessRepository: TenderProcessRepository,
    private val rulesService: RulesService
) {

    fun execute(params: CheckTenderStateParams): ValidationResult<Fail> {
        val cpid = params.cpid
        val ocid = params.ocid

        val tenderEntity = tenderProcessRepository.getByCpIdAndStage(cpid = cpid, stage = ocid.stage)
            .doReturn { fail -> return ValidationResult.error(fail) }
            ?: return ValidationErrors.TenderNotFoundOnCheckTenderState(cpid = cpid, ocid = ocid)
                .asValidationFailure()

        val currentTenderState = tenderEntity.jsonData
            .tryToObject(TenderStateInfo::class.java)
            .map { TenderStatesRule.State(it.tender.status, it.tender.statusDetails) }
            .doReturn { error -> return DatabaseIncident(exception = error.exception).asValidationFailure() }

        val allowedStates = rulesService.getTenderStates(
            pmd = params.pmd,
            country = params.country,
            operationType = params.operationType
        )
            .doReturn { fail -> return fail.asValidationFailure() }

        allowedStates
            .find { it.status == currentTenderState.status && it.statusDetails == currentTenderState.statusDetails }
            ?: return ValidationErrors.TenderStatesIsInvalidOnCheckTenderState(cpid = params.cpid, stage = params.ocid.stage)
                .asValidationFailure()

        return ValidationResult.ok()
    }
}
