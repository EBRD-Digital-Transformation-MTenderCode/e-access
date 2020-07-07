package com.procurement.access.application.service.tender.strategy.set.statefortender

import com.procurement.access.application.model.params.SetStateForTenderParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.set.statefortender.SetStateForTenderResult
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.tryToObject

class SetStateForTenderStrategy(
    private val tenderProcessRepository: TenderProcessRepository
) {
    fun execute(params: SetStateForTenderParams): Result<SetStateForTenderResult, Fail> {

        val tenderProcessEntity = getTenderProcessEntityByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid)
            .doOnError { error -> return Result.failure(error) }
            .get

        val tenderProcess = tenderProcessEntity.jsonData
            .tryToObject(CNEntity::class.java)
            .doOnError { error -> return Result.failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }
            .get

        val updatedTenderProcess = tenderProcess.setTenderStatuses(tender = params.tender)
        tenderProcessRepository.save(tenderProcessEntity.copy(jsonData = toJson(updatedTenderProcess)))
        return SetStateForTenderResult(
            status = updatedTenderProcess.tender.status,
            statusDetails = updatedTenderProcess.tender.statusDetails
        )
            .asSuccess()
    }

    private fun CNEntity.setTenderStatuses(tender: SetStateForTenderParams.Tender): CNEntity =
        this.copy(
            tender = this.tender.copy(
                status = tender.status,
                statusDetails = tender.statusDetails
            )
        )

    private fun getTenderProcessEntityByCpIdAndOcid(cpid: Cpid, ocid: Ocid): Result<TenderProcessEntity, Fail> {
        val entity = tenderProcessRepository.getByCpIdAndStage(cpid = cpid, stage = ocid.stage)
            .doOnError { error -> return Result.failure(error) }
            .get
            ?: return Result.failure(ValidationErrors.TenderNotFoundSetStateForTender(cpid = cpid, ocid = ocid))

        return Result.success(entity)
    }
}
