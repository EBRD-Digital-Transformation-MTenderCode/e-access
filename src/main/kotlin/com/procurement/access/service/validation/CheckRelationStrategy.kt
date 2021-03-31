package com.procurement.access.service.validation

import com.procurement.access.application.model.params.CheckRelationParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.entity.process.RelatedProcess
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.utils.tryToObject

sealed class CheckRelationStrategy {

    object RelationApStrategy : CheckRelationStrategy() {
        override fun check(
            tenderProcessRepository: TenderProcessRepository,
            params: CheckRelationParams,
            stage: ValidationService.StageForCheckingRelation
        ): ValidationResult<Fail> {
            val relatedProcesses = super.getRelatedProcess(tenderProcessRepository, params.cpid, params.ocid, stage)
                .onFailure { return it.reason.asValidationFailure() }

            return if (params.existenceRelation)
                super.checkRelationExists(relatedProcesses, params)
            else
                super.checkRelationNotExists(relatedProcesses, params)
        }
    }

    object CreateRfqStrategy : CheckRelationStrategy() {
        override fun check(
            tenderProcessRepository: TenderProcessRepository,
            params: CheckRelationParams,
            stage: ValidationService.StageForCheckingRelation
        ): ValidationResult<Fail> {
            val relatedProcesses = super.getRelatedProcess(tenderProcessRepository, params.cpid, params.ocid, stage)
                .onFailure { return it.reason.asValidationFailure() }

            return if (params.existenceRelation)
                super.checkRelationExists(relatedProcesses, params)
            else ValidationResult.ok()
        }
    }

    abstract fun check(
        tenderProcessRepository: TenderProcessRepository,
        params: CheckRelationParams,
        stage: ValidationService.StageForCheckingRelation
    ): ValidationResult<Fail>

    private fun getRelatedProcess(
        tenderProcessRepository: TenderProcessRepository,
        cpid: Cpid,
        ocid: Ocid,
        stage: ValidationService.StageForCheckingRelation
    ): Result<List<RelatedProcess>?, Fail> {
        val entity = tenderProcessRepository
            .getByCpIdAndOcid(cpid, ocid)
            .onFailure { return it }
            ?: return ValidationErrors.TenderNotFoundOnCheckRelation(cpid, ocid).asFailure()

        val relatedProcesses = when (stage) {

            ValidationService.StageForCheckingRelation.AP -> entity.jsonData.tryToObject(APEntity::class.java)
                .onFailure { return it }
                .relatedProcesses

            ValidationService.StageForCheckingRelation.PN -> entity.jsonData.tryToObject(PNEntity::class.java)
                .onFailure { return it }
                .relatedProcesses
        }

        return relatedProcesses.asSuccess()
    }

    private val checkRelationExistsPredicate: (RelatedProcess, Cpid) -> Boolean = { relatedProcess, cpid ->
        relatedProcess.relationship.any { it == RelatedProcessType.FRAMEWORK }
            && relatedProcess.identifier == cpid.toString()
    }

    private fun checkRelationExists(
        relatedProcesses: List<RelatedProcess>?,
        params: CheckRelationParams
    ): ValidationResult<ValidationErrors> {

        if (relatedProcesses == null || relatedProcesses.isEmpty())
            return ValidationResult.error(
                ValidationErrors.RelatedProcessNotExistsOnCheckRelation(params.cpid, params.ocid)
            )
        else {
            val isMissing = relatedProcesses.none { checkRelationExistsPredicate(it, params.relatedCpid) }
            if (isMissing)
                return ValidationResult.error(
                    ValidationErrors.MissingAttributesOnCheckRelation(
                        relatedCpid = params.relatedCpid, cpid = params.cpid, ocid = params.ocid
                    )
                )
        }

        return ValidationResult.ok()
    }

    private val checkRelationNotExistsPredicate: (RelatedProcess, Cpid) -> Boolean = { relatedProcess, cpid ->
        relatedProcess.relationship.any { it == RelatedProcessType.X_SCOPE }
            && relatedProcess.identifier == cpid.toString()
    }

    private fun checkRelationNotExists(
        relatedProcesses: List<RelatedProcess>?,
        params: CheckRelationParams
    ): ValidationResult<ValidationErrors> {

        if (relatedProcesses == null || relatedProcesses.isEmpty())
            return ValidationResult.ok()
        else {
            relatedProcesses.forEach { relatedProcess ->
                if (checkRelationNotExistsPredicate(relatedProcess, params.relatedCpid))
                    return ValidationResult.error(
                        ValidationErrors.UnexpectedAttributesValueOnCheckRelation(
                            id = relatedProcess.id,
                            relatedCpid = params.relatedCpid,
                            cpid = params.cpid,
                            ocid = params.ocid
                        )
                    )
            }
        }

        return ValidationResult.ok()
    }
}