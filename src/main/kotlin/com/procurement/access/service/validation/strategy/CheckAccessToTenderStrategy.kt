package com.procurement.access.service.validation.strategy

import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.tender.strategy.check.CheckAccessToTenderParams
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.cpid
import com.procurement.access.infrastructure.api.v1.owner
import com.procurement.access.infrastructure.api.v1.token
import com.procurement.access.infrastructure.repository.CassandraTenderProcessRepositoryV1
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.model.entity.TenderProcessEntity

class CheckAccessToTenderStrategy(
    private val tenderRepository: CassandraTenderProcessRepositoryV1,
    private val tenderProcessRepository: TenderProcessRepository
) {

    fun checkAccessToTender(cm: CommandMessage) {
        val cpid = cm.cpid
        val token = cm.token
        val owner = cm.owner

        val auths = tenderRepository.findAuthByCpid(cpid = cpid)
            .takeIf { it.isNotEmpty() }
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)


        auths.forEach { auth ->
            if (auth.owner != owner)
                throw ErrorException(error = ErrorType.INVALID_OWNER)

            if (auth.token != token)
                throw ErrorException(error = ErrorType.INVALID_TOKEN)
        }
    }

    fun checkAccessToTender(params: CheckAccessToTenderParams): ValidationResult<Fail> {
        checkStage(params)
            .doOnError { return it.asValidationFailure() }

        val tenderProcessEntity = getTenderProcessEntityByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid)
            .onFailure { return it.reason.asValidationFailure() }

        if (tenderProcessEntity.owner != params.owner.toString())
            return ValidationResult.error(ValidationErrors.InvalidOwner(owner = params.owner, cpid = params.cpid))

        if (tenderProcessEntity.token != params.token)
            return ValidationResult.error(ValidationErrors.InvalidToken(token = params.token, cpid = params.cpid))

        return ValidationResult.ok()
    }

    private fun checkStage(params: CheckAccessToTenderParams): ValidationResult<ValidationErrors.InvalidStageCheckAccessToTender> =
        when (val stage = params.ocid.stage) {
            Stage.PN,
            Stage.AP,
            Stage.EV,
            Stage.NP,
            Stage.TP,
            Stage.FE,
            Stage.RQ -> ValidationResult.ok()

            Stage.AC,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PO -> ValidationResult.error(ValidationErrors.InvalidStageCheckAccessToTender(stage))
        }

    private fun getTenderProcessEntityByCpIdAndOcid(cpid: Cpid, ocid: Ocid.SingleStage): Result<TenderProcessEntity, Fail> {
        val entity = tenderProcessRepository.getByCpIdAndOcid(cpid = cpid, ocid = ocid)
            .onFailure { return it }
            ?: return Result.failure(ValidationErrors.TenderNotFoundCheckAccessToTender(cpid = cpid, ocid = ocid))

        return Result.success(entity)
    }
}
