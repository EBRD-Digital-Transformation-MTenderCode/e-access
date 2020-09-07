package com.procurement.access.service

import com.procurement.access.application.model.params.OutsourcingPNParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.Logger
import com.procurement.access.config.properties.UriProperties
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.RelatedProcessScheme
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.process.RelatedProcessId
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.Result.Companion.success
import com.procurement.access.domain.util.extension.mapResult
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.entity.process.RelatedProcess
import com.procurement.access.infrastructure.handler.pn.OutsourcingPNResult
import com.procurement.access.utils.trySerialization
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface OutsourcingService {
    fun outsourcingPN(params: OutsourcingPNParams): Result<OutsourcingPNResult, Fail>
}

@Service
class OutsourcingServiceImpl(
    private val tenderProcessRepository: TenderProcessRepository,
    private val uriProperties: UriProperties,
    private val logger: Logger
) : OutsourcingService {

    override fun outsourcingPN(params: OutsourcingPNParams): Result<OutsourcingPNResult, Fail> {

        val entity = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
            .orForwardFail { fail -> return fail }
            ?: return failure(
                ValidationErrors.TenderNotFoundOnOutsourcingPN(params.cpid, params.ocid)
            )

        val pnEntity = entity.jsonData.tryToObject(PNEntity::class.java)
            .orForwardFail { fail -> return fail }

        val relatedProcesses = listOf(
            RelatedProcess(
                id = RelatedProcessId.randomUUID(), // FR.COM-1.21.1
                relationship = listOf(RelatedProcessType.FRAMEWORK), // FR.COM-1.21.2
                scheme = RelatedProcessScheme.OCID, // FR.COM-1.21.3
                identifier = params.cpidFA, // FR.COM-1.21.4
                uri = "${uriProperties.tender}/${params.cpidFA}/${params.cpidFA}" //FR.COM-1.21.5
            )
        )
        val updatedPn = pnEntity.copy(relatedProcesses = relatedProcesses)

        val response = OutsourcingPNResult(
            relatedProcesses = relatedProcesses
                .mapResult { OutsourcingPNResult.fromDomain(it) }
                .orForwardFail { fail -> return fail }
        )

        val updatedJsonData = trySerialization(updatedPn)
            .orForwardFail { fail -> return fail }

        val updatedEntity = entity.copy(jsonData = updatedJsonData)

        val wasApplied = tenderProcessRepository.update(updatedEntity)
            .orForwardFail { fail -> return fail }

        if (!wasApplied) {
            logger.error(
                "Cannot update record " + "(cpid=${updatedEntity.cpId}, stage=${updatedEntity.stage}). " +
                    "Data for update='${updatedEntity.jsonData}'"
            )
            return failure(Fail.Incident.DatabaseIncident())
        }

        return success(response)
    }
}
