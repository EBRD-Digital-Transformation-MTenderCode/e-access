package com.procurement.access.service

import com.procurement.access.application.model.params.CreateRelationToOtherProcessParams
import com.procurement.access.application.model.params.OutsourcingPNParams
import com.procurement.access.application.model.parseOcid
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.Logger
import com.procurement.access.config.properties.UriProperties
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.RelatedProcessScheme
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.process.RelatedProcessId
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.entity.RelatedProcessesInfo
import com.procurement.access.infrastructure.entity.process.RelatedProcess
import com.procurement.access.infrastructure.handler.create.relation.CreateRelationToOtherProcessResult
import com.procurement.access.infrastructure.handler.pn.OutsourcingPNResult
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.extension.toList
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import com.procurement.access.lib.functional.Result.Companion.success
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.trySerialization
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface OutsourcingService {
    fun outsourcingPN(params: OutsourcingPNParams): Result<OutsourcingPNResult, Fail>
    fun createRelationToOtherProcess(params: CreateRelationToOtherProcessParams): Result<CreateRelationToOtherProcessResult, Fail>
}

@Service
class OutsourcingServiceImpl(
    private val tenderProcessRepository: TenderProcessRepository,
    private val uriProperties: UriProperties,
    private val logger: Logger
) : OutsourcingService {

    override fun outsourcingPN(params: OutsourcingPNParams): Result<OutsourcingPNResult, Fail> {

        val entity = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
            .onFailure { fail -> return fail }
            ?: return failure(
                ValidationErrors.TenderNotFoundOnOutsourcingPN(params.cpid, params.ocid)
            )

        val pnEntity = entity.jsonData.tryToObject(PNEntity::class.java)
            .onFailure { fail -> return fail }

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
                .onFailure { fail -> return fail }
        )

        val updatedJsonData = trySerialization(updatedPn)
            .onFailure { fail -> return fail }

        val updatedEntity = entity.copy(jsonData = updatedJsonData)

        tenderProcessRepository.update(updatedEntity)
            .onFailure { fail -> return fail }

        return success(response)
    }

    companion object CreateRelationToOtherProcess {
        fun defineRelationProcessType(operationType: OperationType): Result<RelatedProcessType, DataErrors.Validation.UnknownValue> =
            when (operationType) {
                OperationType.OUTSOURCING_PN -> success(RelatedProcessType.X_DEMAND)
                OperationType.RELATION_AP -> success(RelatedProcessType.X_SCOPE)

                OperationType.AMEND_FE,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_FE,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
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
                OperationType.UPDATE_AP,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_QUALIFICATION_PROTOCOL ->
                    failure(
                        DataErrors.Validation.UnknownValue(
                            name = "operationType",
                            actualValue = operationType.toString(),
                            expectedValues = CreateRelationToOtherProcessParams.allowedOperationType
                                .map { it.toString() }
                        )
                    )
            }

        fun isNeedToFindStoredRelatedProcess(operationType: OperationType): Boolean =
            when (operationType) {
                OperationType.RELATION_AP -> true

                OperationType.AMEND_FE,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_FE,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_SUBMISSION,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMISSION_PERIOD_END,
                OperationType.TENDER_PERIOD_END,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> false
            }

        fun getTenderEntity(
            tenderProcessRepository: TenderProcessRepository,
            params: CreateRelationToOtherProcessParams
        ): Result<TenderProcessEntity, Fail> {
            val ocid = parseOcid(params.ocid).onFailure { return it }

            val entity = tenderProcessRepository.getByCpIdAndStage(params.cpid, ocid.stage)
                .onFailure { fail -> return fail }
                ?: return failure(
                    ValidationErrors.TenderNotFoundOnCreateRelationToOtherProcess(params.cpid, ocid)
                )

            return success(entity)
        }
    }

    override fun createRelationToOtherProcess(params: CreateRelationToOtherProcessParams): Result<CreateRelationToOtherProcessResult, Fail> {

        val isNeedToFindStoredRelatedProcess = isNeedToFindStoredRelatedProcess(params.operationType)

        if (isNeedToFindStoredRelatedProcess) {
            val storedRelatedProcess = findStoredRelatedProcess(params)
                .onFailure { return it }

            if (storedRelatedProcess != null)
                return storedRelatedProcess.asSuccess()
        }

        val definedRelationship = defineRelationProcessType(params.operationType)
            .onFailure { fail -> return fail }

        val relatedProcesses = listOf(
            RelatedProcess(
                id = RelatedProcessId.randomUUID(), // FR.COM-1.22.1
                relationship = listOf(definedRelationship), // FR.COM-1.22.2
                scheme = RelatedProcessScheme.OCID, // FR.COM-1.22.3
                identifier = params.relatedCpid.toString(), // FR.COM-1.22.4
                uri = "${uriProperties.tender}/${params.relatedCpid}/${params.relatedCpid}" //FR.COM-1.22.5
            )
        )

        val response = CreateRelationToOtherProcessResult(
            relatedProcesses = relatedProcesses
                .mapResult { CreateRelationToOtherProcessResult.fromDomain(it) }
                .onFailure { fail -> return fail })

        when (params.operationType) {
            OperationType.RELATION_AP -> { // FR.COM-1.22.6
                val entity = getTenderEntity(tenderProcessRepository, params)
                    .onFailure { fail -> return fail }

                entity.jsonData
                    .tryToObject(APEntity::class.java)
                    .map { ap -> ap.copy(relatedProcesses = ap.relatedProcesses.orEmpty() + relatedProcesses) }
                    .flatMap { updatedAp -> trySerialization(updatedAp) }
                    .map { updatedApJson -> entity.copy(jsonData = updatedApJson) }
                    .flatMap { updatedEntity -> tenderProcessRepository.update(updatedEntity) }
                    .onFailure { fail -> return fail }
            }
            OperationType.AMEND_FE,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.COMPLETE_QUALIFICATION,
            OperationType.CREATE_CN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_CN_ON_PN,
            OperationType.CREATE_FE,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN,
            OperationType.CREATE_PCR,
            OperationType.CREATE_PIN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_PN,
            OperationType.CREATE_SUBMISSION,
            OperationType.OUTSOURCING_PN,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_PROTOCOL,
            OperationType.START_SECONDSTAGE,
            OperationType.SUBMISSION_PERIOD_END,
            OperationType.TENDER_PERIOD_END,
            OperationType.UPDATE_AP,
            OperationType.UPDATE_CN,
            OperationType.UPDATE_PN,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> Unit
        }

        // FR.COM-1.22.7
        return success(response)
    }

    private fun findStoredRelatedProcess(params: CreateRelationToOtherProcessParams): Result<CreateRelationToOtherProcessResult?, Fail> {
        val ocid = parseOcid(params.ocid).onFailure { return it }

        val tenderEntity = tenderProcessRepository.getByCpIdAndStage(params.cpid, ocid.stage)
            .onFailure { fail -> return fail }
            ?: return null.asSuccess()

        val relatedProcess = tenderEntity.jsonData
            .tryToObject(RelatedProcessesInfo::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(it.exception) }
            .onFailure { return it }

        val suitableRelatedProcess = relatedProcess.relatedProcesses?.firstOrNull { it.identifier == params.relatedCpid.toString() }

        return suitableRelatedProcess?.let {
            CreateRelationToOtherProcessResult(
                relatedProcesses = CreateRelationToOtherProcessResult
                    .fromDomain(it)
                    .onFailure { fail -> return fail }
                    .toList()
            )
        }.asSuccess()
    }
}
