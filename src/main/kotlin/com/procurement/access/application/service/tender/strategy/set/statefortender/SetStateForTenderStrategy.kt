package com.procurement.access.application.service.tender.strategy.set.statefortender

import com.procurement.access.application.model.params.SetStateForTenderParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.rule.TenderStatesRule
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.handler.set.statefortender.SetStateForTenderResult
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.tryToObject

class SetStateForTenderStrategy(
    private val tenderProcessRepository: TenderProcessRepository
) {
    fun execute(params: SetStateForTenderParams): Result<SetStateForTenderResult, Fail> {

        val tenderProcessEntity = getTenderProcessEntityByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid)
            .orForwardFail { error -> return error }

        val newState = TenderStatesRule.State(
            status = params.tender.status,
            statusDetails = params.tender.statusDetails
        )

        when (params.ocid.stage) {
            Stage.PN -> tenderProcessEntity.jsonData
                .tryToObject(PNEntity::class.java)
                .doReturn { error -> return Result.failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }
                .let {
                    it.copy(
                        tender = it.tender.copy(status = newState.status, statusDetails = newState.statusDetails)
                    )
                }
                .also { updatedTenderProcess ->
                    tenderProcessRepository.save(tenderProcessEntity.copy(jsonData = toJson(updatedTenderProcess)))
                        .orForwardFail { error -> return error }
                }


            Stage.AP -> tenderProcessEntity.jsonData
                .tryToObject(APEntity::class.java)
                .doReturn { error -> return Result.failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }
                .let {
                    it.copy(
                        tender = it.tender.copy(status = newState.status, statusDetails = newState.statusDetails)
                    )
                }
                .also { updatedTenderProcess ->
                    tenderProcessRepository.save(tenderProcessEntity.copy(jsonData = toJson(updatedTenderProcess)))
                        .orForwardFail { error -> return error }
                }

            Stage.EV,
            Stage.TP,
            Stage.NP -> tenderProcessEntity.jsonData
                .tryToObject(CNEntity::class.java)
                .doReturn { error -> return Result.failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }
                .let {
                    it.copy(
                        tender = it.tender.copy(status = newState.status, statusDetails = newState.statusDetails)
                    )
                }
                .also { updatedTenderProcess ->
                    tenderProcessRepository.save(tenderProcessEntity.copy(jsonData = toJson(updatedTenderProcess)))
                        .orForwardFail { error -> return error }
                }

            Stage.EI,
            Stage.FS,
            Stage.AC ->
                return Result.failure(
                    DataErrors.Validation.UnknownValue(
                        name = "stage",
                        expectedValues = listOf(Stage.PN, Stage.AP, Stage.EV, Stage.TP, Stage.NP).map { it.toString() },
                        actualValue = params.ocid.stage.toString()
                    )
                )
        }

        return SetStateForTenderResult(
            status = newState.status,
            statusDetails = newState.statusDetails
        )
            .asSuccess()
    }

    private fun getTenderProcessEntityByCpIdAndOcid(cpid: Cpid, ocid: Ocid): Result<TenderProcessEntity, Fail> {
        val entity = tenderProcessRepository.getByCpIdAndStage(cpid = cpid, stage = ocid.stage)
            .orForwardFail { error -> return error }
            ?: return Result.failure(ValidationErrors.TenderNotFoundSetStateForTender(cpid = cpid, ocid = ocid))

        return Result.success(entity)
    }
}
