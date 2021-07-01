package com.procurement.access.application.service.tender.strategy.set.statefortender

import com.procurement.access.application.model.params.SetStateForTenderParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.rule.TenderStatesRule
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.entity.RfqEntity
import com.procurement.access.infrastructure.handler.v2.model.response.SetStateForTenderResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.tryToObject

class SetStateForTenderStrategy(
    private val tenderProcessRepository: TenderProcessRepository
) {

    fun execute(params: SetStateForTenderParams): Result<SetStateForTenderResult, Fail> {

        val tenderProcessEntity = getTenderProcessEntityByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid)
            .onFailure { error -> return error }

        val newState = TenderStatesRule.State(
            status = params.tender.status,
            statusDetails = params.tender.statusDetails
        )

        val updatedTenderJson = when (params.ocid.stage) {
            Stage.PN -> tenderProcessEntity.jsonData
                .tryToObject(PNEntity::class.java)
                .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                .onFailure { return it }
                .let {
                    it.copy(
                        tender = it.tender.copy(status = newState.status, statusDetails = newState.statusDetails)
                    )
                }
                .let { updatedTenderProcess -> toJson(updatedTenderProcess) }

            Stage.AP -> tenderProcessEntity.jsonData
                .tryToObject(APEntity::class.java)
                .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                .onFailure { return it }
                .let {
                    it.copy(tender = it.tender.copy(status = newState.status, statusDetails = newState.statusDetails))
                }
                .let { updatedTenderProcess -> toJson(updatedTenderProcess) }

            Stage.FE -> tenderProcessEntity.jsonData
                .tryToObject(FEEntity::class.java)
                .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                .onFailure { return it }
                .let {
                    it.copy(tender = it.tender.copy(status = newState.status, statusDetails = newState.statusDetails))
                }
                .let { updatedTenderProcess -> toJson(updatedTenderProcess) }

            Stage.EV,
            Stage.TP,
            Stage.NP -> tenderProcessEntity.jsonData
                .tryToObject(CNEntity::class.java)
                .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                .onFailure { return it }
                .let {
                    it.copy(tender = it.tender.copy(status = newState.status, statusDetails = newState.statusDetails))
                }
                .let { updatedTenderProcess -> toJson(updatedTenderProcess) }
            Stage.RQ -> tenderProcessEntity.jsonData
                .tryToObject(RfqEntity::class.java)
                .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                .onFailure { return it }
                .let {
                    it.copy(tender = it.tender.copy(status = newState.status, statusDetails = newState.statusDetails))
                }
                .let { updatedTenderProcess -> toJson(updatedTenderProcess) }

            Stage.AC,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PO ->
                return Result.failure(ValidationErrors.UnexpectedStageForSetStateForTender(stage = params.ocid.stage))
        }

        tenderProcessRepository.save(tenderProcessEntity.copy(jsonData = updatedTenderJson))
            .onFailure { error -> return error }

        return SetStateForTenderResult(
            status = newState.status,
            statusDetails = newState.statusDetails
        )
            .asSuccess()
    }

    private fun getTenderProcessEntityByCpIdAndOcid(cpid: Cpid, ocid: Ocid.SingleStage): Result<TenderProcessEntity, Fail> {
        val entity = tenderProcessRepository.getByCpIdAndOcid(cpid = cpid, ocid = ocid)
            .onFailure { error -> return error }
            ?: return Result.failure(ValidationErrors.TenderNotFoundSetStateForTender(cpid = cpid, ocid = ocid))

        return Result.success(entity)
    }
}
