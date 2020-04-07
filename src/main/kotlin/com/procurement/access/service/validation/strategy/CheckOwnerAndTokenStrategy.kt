package com.procurement.access.service.validation.strategy

import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.tender.strategy.check.CheckAccessToTenderParams
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.cpid
import com.procurement.access.model.dto.bpe.owner
import com.procurement.access.model.dto.bpe.token
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.tryToObject

class CheckOwnerAndTokenStrategy(
    private val tenderProcessDao: TenderProcessDao,
    private val tenderProcessRepository: TenderProcessRepository
) {

    fun checkOwnerAndToken(cm: CommandMessage) {
        val cpid = cm.cpid
        val token = cm.token
        val owner = cm.owner

        val auths = tenderProcessDao.findAuthByCpid(cpid = cpid)
            .takeIf { it.isNotEmpty() }
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)


        auths.forEach { auth ->
            if (auth.owner != owner)
                throw ErrorException(error = ErrorType.INVALID_OWNER)

            if (auth.token != token)
                throw ErrorException(error = ErrorType.INVALID_TOKEN)
        }
    }

    fun checkOwnerAndToken(params: CheckAccessToTenderParams): ValidationResult<Fail> {

        val tenderProcessEntity = getTenderProcessEntityByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid)
            .doOnError { error -> return ValidationResult.error(error) }
            .get

        val tenderProcess = tenderProcessEntity.jsonData
            .tryToObject(TenderProcess::class.java)
            .doOnError { error ->
                return ValidationResult.error(Fail.Incident.DatabaseIncident(exception = error.exception))
            }
            .get

        if (tenderProcess.ocid != params.ocid.toString())
            return ValidationResult.error(
                ValidationErrors.TenderNotFoundCheckAccessToTender(cpid = params.cpid, ocid = params.ocid)
            )

        if (tenderProcessEntity.owner != params.owner)
            return ValidationResult.error(ValidationErrors.InvalidOwner(owner = params.owner, cpid = params.cpid))

        if (tenderProcessEntity.token != params.token)
            return ValidationResult.error(ValidationErrors.InvalidToken(token = params.token, cpid = params.cpid))

        return ValidationResult.ok()
    }

    private fun getTenderProcessEntityByCpIdAndOcid(cpid: Cpid, ocid: Ocid): Result<TenderProcessEntity, Fail> {
        val entity = tenderProcessRepository.getByCpIdAndStage(cpid = cpid, stage = ocid.stage)
            .doOnError { error -> return Result.failure(error) }
            .get
            ?: return Result.failure(ValidationErrors.TenderNotFoundCheckAccessToTender(cpid = cpid, ocid = ocid))

        return Result.success(entity)
    }
}
