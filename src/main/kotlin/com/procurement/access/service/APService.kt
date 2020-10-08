package com.procurement.access.service

import com.procurement.access.application.model.params.CalculateAPValueParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.money.Money
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.Result.Companion.success
import com.procurement.access.domain.util.bind
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.entity.process.RelatedProcess
import com.procurement.access.infrastructure.handler.calculate.value.CalculateAPValueResult
import com.procurement.access.utils.trySerialization
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service
import java.math.BigDecimal

interface APService {
    fun calculateAPValue(params: CalculateAPValueParams): Result<CalculateAPValueResult, Fail>
}

@Service
class APServiceImpl(
    private val tenderProcessRepository: TenderProcessRepository,
    private val logger: Logger
) : APService {

    override fun calculateAPValue(params: CalculateAPValueParams): Result<CalculateAPValueResult, Fail> {

        // FR.COM-1.31.1
        val entity = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
            .orForwardFail { fail -> return fail }
            ?: return failure( // VR.COM-1.31.1
                ValidationErrors.TenderNotFoundOnCalculateAPValue(params.cpid, params.ocid)
            )

        val ap = entity.jsonData.tryToObject(APEntity::class.java)
            .orForwardFail { fail -> return fail }

        // FR.COM-1.31.2
        val relatedPNProcesses = ap.relatedProcesses.orEmpty()
            .filter(::isRelatedToPN)

        // VR.COM-1.31.2
        if (relatedPNProcesses.isEmpty())
            return failure(ValidationErrors.RelationNotFoundOnCalculateAPValue(params.cpid, params.ocid))

        val relatedPns = relatedPNProcesses.map { pnProcess ->
            parseCpid(pnProcess.identifier)
                .bind { parsedCpid -> tenderProcessRepository.getByCpIdAndStage(parsedCpid, Stage.PN) }
                .bind { pnEntity -> pnEntity!!.jsonData.tryToObject(PNEntity::class.java) }
                .orForwardFail { fail -> return fail }
        }

        // FR.COM-1.31.3
        // FR.COM-1.31.4
        val relatedPnsValueSum = relatedPns
            .map { it.tender.value.amount }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        // FR.COM-1.31.5
        val apCurrency = relatedPns.first().tender.value.currency

        val apTenderValue = Money(amount = relatedPnsValueSum, currency = apCurrency)

        val updatedAp = ap.copy(
            tender = ap.tender.copy(
                value = apTenderValue
            )
        )

        val updatedJsonData = trySerialization(updatedAp)
            .orForwardFail { fail -> return fail }

        val updatedEntity = entity.copy(jsonData = updatedJsonData)

        // FR.COM-1.31.6
        tenderProcessRepository.update(entity = updatedEntity)

        val result = CalculateAPValueResult(CalculateAPValueResult.Tender(apTenderValue))

        return success(result)
    }

    private fun isRelatedToPN(relatedProcess: RelatedProcess): Boolean =
        relatedProcess.relationship.any { relationship -> relationship == RelatedProcessType.X_SCOPE }

}
