package com.procurement.access.service

import com.procurement.access.application.model.params.CreateRelationToOtherProcessParams
import com.procurement.access.application.model.params.OutsourcingPNParams
import com.procurement.access.application.model.parseOcid
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.RelatedProcessScheme
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.process.RelatedProcessId
import com.procurement.access.domain.model.process.RelatedProcessIdentifier
import com.procurement.access.infrastructure.configuration.properties.UriProperties
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.entity.RelatedProcessesInfo
import com.procurement.access.infrastructure.entity.process.RelatedProcess
import com.procurement.access.infrastructure.handler.v2.model.response.CreateRelationToOtherProcessResult
import com.procurement.access.infrastructure.handler.v2.model.response.OutsourcingPNResult
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.extension.toList
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import com.procurement.access.lib.functional.Result.Companion.success
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.asValidationFailure
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
                identifier = RelatedProcessIdentifier.of(params.cpidFA), // FR.COM-1.21.4
                uri = "${uriProperties.tender}/${params.cpidFA.value}/${params.cpidFA.value}" //FR.COM-1.21.5
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
                OperationType.CREATE_PCR -> success(RelatedProcessType.X_PCR)
                OperationType.OUTSOURCING_PN -> success(RelatedProcessType.X_DEMAND)
                OperationType.RELATION_AP -> success(RelatedProcessType.X_SCOPE)

                OperationType.AMEND_FE,
                OperationType.APPLY_QUALIFICATION_PROTOCOL,
                OperationType.AWARD_CONSIDERATION,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_AWARD,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
                OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE, 
                OperationType.CREATE_FE,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_RFQ,
                OperationType.CREATE_SUBMISSION,
                OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.DIVIDE_LOT,
                OperationType.ISSUING_FRAMEWORK_CONTRACT,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMISSION_PERIOD_END,
                OperationType.SUBMIT_BID,
                OperationType.TENDER_PERIOD_END,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_AWARD,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_BID,
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
                OperationType.AWARD_CONSIDERATION,
                OperationType.COMPLETE_QUALIFICATION,
                OperationType.CREATE_AWARD,
                OperationType.CREATE_CN,
                OperationType.CREATE_CN_ON_PIN,
                OperationType.CREATE_CN_ON_PN,
                OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
                OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE,
                OperationType.CREATE_FE,
                OperationType.CREATE_NEGOTIATION_CN_ON_PN,
                OperationType.CREATE_PCR,
                OperationType.CREATE_PIN,
                OperationType.CREATE_PIN_ON_PN,
                OperationType.CREATE_PN,
                OperationType.CREATE_RFQ,
                OperationType.CREATE_SUBMISSION,
                OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.DIVIDE_LOT,
                OperationType.ISSUING_FRAMEWORK_CONTRACT,
                OperationType.OUTSOURCING_PN,
                OperationType.QUALIFICATION,
                OperationType.QUALIFICATION_CONSIDERATION,
                OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
                OperationType.QUALIFICATION_PROTOCOL,
                OperationType.START_SECONDSTAGE,
                OperationType.SUBMISSION_PERIOD_END,
                OperationType.SUBMIT_BID,
                OperationType.TENDER_PERIOD_END,
                OperationType.UPDATE_AP,
                OperationType.UPDATE_AWARD,
                OperationType.UPDATE_CN,
                OperationType.UPDATE_PN,
                OperationType.WITHDRAW_BID,
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

        checkRelatedOcidPresence(params)
            .doOnError { return it.asFailure() }

        val definedRelationship = defineRelationProcessType(params.operationType)
            .onFailure { fail -> return fail }

        val definedIdentifier = defineIdentifier(params)
            .onFailure { fail -> return fail }

        val definedUri = defineUri(params)
            .onFailure { fail -> return fail }

        val relatedProcesses = listOf(
            RelatedProcess(
                id = RelatedProcessId.randomUUID(), // FR.COM-1.22.1
                relationship = listOf(definedRelationship), // FR.COM-1.22.2
                scheme = RelatedProcessScheme.OCID, // FR.COM-1.22.3
                identifier = definedIdentifier, // FR.COM-1.22.4
                uri = definedUri //FR.COM-1.22.5
            )
        )

        val response = CreateRelationToOtherProcessResult(
            relatedProcesses = relatedProcesses
                .mapResult { CreateRelationToOtherProcessResult.fromDomain(it) }
                .onFailure { fail -> return fail })

        when (params.operationType) {
            OperationType.RELATION_AP -> {
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
            OperationType.CREATE_PCR -> { // FR.COM-1.22.6
                val entity = getTenderEntity(tenderProcessRepository, params)
                    .onFailure { fail -> return fail }

                entity.jsonData
                    .tryToObject(FEEntity::class.java)
                    .map { fe -> fe.copy(relatedProcesses = fe.relatedProcesses.orEmpty() + relatedProcesses) }
                    .flatMap { updatedFE -> trySerialization(updatedFE) }
                    .map { updatedFEJson -> entity.copy(jsonData = updatedFEJson) }
                    .flatMap { updatedEntity -> tenderProcessRepository.update(updatedEntity) }
                    .onFailure { fail -> return fail }
            }
            OperationType.AMEND_FE,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.COMPLETE_QUALIFICATION,
            OperationType.CREATE_AWARD,
            OperationType.CREATE_CN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_CN_ON_PN,
            OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
            OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE,
            OperationType.CREATE_FE,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN,
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
            OperationType.UPDATE_AWARD,
            OperationType.UPDATE_CN,
            OperationType.UPDATE_PN,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> Unit
        }

        // FR.COM-1.22.7
        return success(response)
    }

    private fun checkRelatedOcidPresence(params: CreateRelationToOtherProcessParams): ValidationResult<Fail> =
        when (params.operationType) {
            OperationType.CREATE_PCR ->
                if (params.relatedOcid == null)
                    ValidationErrors.RelatedOcidIsAbsent().asValidationFailure()
                else ValidationResult.ok()

            OperationType.AMEND_FE,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.AWARD_CONSIDERATION,
            OperationType.COMPLETE_QUALIFICATION,
            OperationType.CREATE_AWARD,
            OperationType.CREATE_CN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_CN_ON_PN,
            OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
            OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE,    
            OperationType.CREATE_FE,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN,
            OperationType.CREATE_PIN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_PN,
            OperationType.CREATE_RFQ,
            OperationType.CREATE_SUBMISSION,
            OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.DIVIDE_LOT,
            OperationType.ISSUING_FRAMEWORK_CONTRACT,
            OperationType.OUTSOURCING_PN,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.QUALIFICATION_PROTOCOL,
            OperationType.RELATION_AP,
            OperationType.START_SECONDSTAGE,
            OperationType.SUBMISSION_PERIOD_END,
            OperationType.SUBMIT_BID,
            OperationType.TENDER_PERIOD_END,
            OperationType.UPDATE_AP,
            OperationType.UPDATE_AWARD,
            OperationType.UPDATE_CN,
            OperationType.UPDATE_PN,
            OperationType.WITHDRAW_BID,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> ValidationResult.ok()
        }



    private fun defineUri(params: CreateRelationToOtherProcessParams): Result<String, DataErrors.Validation.UnknownValue> =
    when (params.operationType) {
        OperationType.CREATE_PCR -> "${uriProperties.tender}/${params.relatedCpid.value}/${params.relatedOcid!!.value}".asSuccess()
        OperationType.OUTSOURCING_PN,
        OperationType.RELATION_AP -> "${uriProperties.tender}/${params.relatedCpid.value}/${params.relatedCpid.value}".asSuccess()

        OperationType.AMEND_FE,
        OperationType.APPLY_QUALIFICATION_PROTOCOL,
        OperationType.AWARD_CONSIDERATION,
        OperationType.COMPLETE_QUALIFICATION,
        OperationType.CREATE_AWARD,
        OperationType.CREATE_CN,
        OperationType.CREATE_CN_ON_PIN,
        OperationType.CREATE_CN_ON_PN,
        OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
        OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE,
        OperationType.CREATE_FE,
        OperationType.CREATE_NEGOTIATION_CN_ON_PN,
        OperationType.CREATE_PIN,
        OperationType.CREATE_PIN_ON_PN,
        OperationType.CREATE_PN,
        OperationType.CREATE_RFQ,
        OperationType.CREATE_SUBMISSION,
        OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
        OperationType.DIVIDE_LOT,
        OperationType.ISSUING_FRAMEWORK_CONTRACT,
        OperationType.QUALIFICATION,
        OperationType.QUALIFICATION_CONSIDERATION,
        OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
        OperationType.QUALIFICATION_PROTOCOL,
        OperationType.START_SECONDSTAGE,
        OperationType.SUBMISSION_PERIOD_END,
        OperationType.SUBMIT_BID,
        OperationType.TENDER_PERIOD_END,
        OperationType.UPDATE_AP,
        OperationType.UPDATE_AWARD,
        OperationType.UPDATE_CN,
        OperationType.UPDATE_PN,
        OperationType.WITHDRAW_BID,
        OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> failure(generateOperationTypeError(params))
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

        val targetIdentifier = RelatedProcessIdentifier.of(params.relatedCpid)
        val suitableRelatedProcess = relatedProcess.relatedProcesses?.firstOrNull { it.identifier == targetIdentifier }

        return suitableRelatedProcess?.let {
            CreateRelationToOtherProcessResult(
                relatedProcesses = CreateRelationToOtherProcessResult
                    .fromDomain(it)
                    .onFailure { fail -> return fail }
                    .toList()
            )
        }.asSuccess()
    }

    private fun defineIdentifier(params: CreateRelationToOtherProcessParams): Result<RelatedProcessIdentifier, DataErrors.Validation.UnknownValue> =
        when (params.operationType) {
            OperationType.CREATE_PCR -> RelatedProcessIdentifier.of(params.relatedOcid!!).asSuccess()
            OperationType.OUTSOURCING_PN,
            OperationType.RELATION_AP -> RelatedProcessIdentifier.of(params.relatedCpid).asSuccess()

            OperationType.AMEND_FE,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.AWARD_CONSIDERATION,
            OperationType.COMPLETE_QUALIFICATION,
            OperationType.CREATE_AWARD,
            OperationType.CREATE_CN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_CN_ON_PN,
            OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
            OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE,
            OperationType.CREATE_FE,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN,
            OperationType.CREATE_PIN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_PN,
            OperationType.CREATE_RFQ,
            OperationType.CREATE_SUBMISSION,
            OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.DIVIDE_LOT,
            OperationType.ISSUING_FRAMEWORK_CONTRACT,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.QUALIFICATION_PROTOCOL,
            OperationType.START_SECONDSTAGE,
            OperationType.SUBMISSION_PERIOD_END,
            OperationType.SUBMIT_BID,
            OperationType.TENDER_PERIOD_END,
            OperationType.UPDATE_AP,
            OperationType.UPDATE_AWARD,
            OperationType.UPDATE_CN,
            OperationType.UPDATE_PN,
            OperationType.WITHDRAW_BID,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> failure(generateOperationTypeError(params))
        }

    private fun generateOperationTypeError(params: CreateRelationToOtherProcessParams): DataErrors.Validation.UnknownValue {
        return DataErrors.Validation.UnknownValue(
            name = "operationType",
            actualValue = params.operationType.toString(),
            expectedValues = CreateRelationToOtherProcessParams.allowedOperationType
                .map { it.toString() }
        )
    }
}
