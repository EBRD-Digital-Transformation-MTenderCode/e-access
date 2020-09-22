package com.procurement.access.application.service.tender.strategy.check.tenderstate

import com.procurement.access.application.model.params.CheckTenderStateParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.Fail.Incident.DatabaseIncident
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.rule.TenderStatesRule
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.domain.util.asValidationFailure
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.PNEntity
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

        val tenderState = when(params.operationType) {
            OperationType.OUTSOURCING_PN ->
                tenderEntity.jsonData
                    .tryToObject(PNEntity::class.java)
                    .map { TenderStatesRule.State(it.tender.status, it.tender.statusDetails) }

            OperationType.UPDATE_AP,
            OperationType.RELATION_AP ->
                tenderEntity.jsonData
                    .tryToObject(APEntity::class.java)
                    .map { TenderStatesRule.State(it.tender.status, it.tender.statusDetails) }

            OperationType.AMEND_FE,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.CREATE_CN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_CN_ON_PN,
            OperationType.CREATE_FE,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN,
            OperationType.CREATE_PIN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_PN,
            OperationType.CREATE_SUBMISSION,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_PROTOCOL,
            OperationType.START_SECONDSTAGE,
            OperationType.SUBMISSION_PERIOD_END,
            OperationType.TENDER_PERIOD_END,
            OperationType.UPDATE_CN,
            OperationType.UPDATE_PN,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL ->
                tenderEntity.jsonData
                    .tryToObject(CNEntity::class.java)
                    .map { TenderStatesRule.State(it.tender.status, it.tender.statusDetails) }
        }
            .doReturn { error ->
                return DatabaseIncident(exception = error.exception).asValidationFailure()
            }

        val allowedStates = rulesService.getTenderStates(
            pmd = params.pmd,
            country = params.country,
            operationType = params.operationType
        )
            .doReturn { fail -> return fail.asValidationFailure() }

        allowedStates
            .find { it.status == tenderState.status && it.statusDetails == tenderState.statusDetails }
            ?: return ValidationErrors.TenderStatesIsInvalidOnCheckTenderState(cpid = params.cpid, stage = params.ocid.stage)
                .asValidationFailure()

        return ValidationResult.ok()
    }
}
