package com.procurement.access.service.validation.strategy

import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.tender.strategy.check.CheckAccessToTenderParams
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.cpid
import com.procurement.access.model.dto.bpe.owner
import com.procurement.access.model.dto.bpe.token

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
        val auths = tenderProcessRepository.findAuthByCpid(cpid = params.cpid)
            .doOnError { error -> return ValidationResult.error(error) }
            .get
        auths.forEach { auth ->
            if (auth.owner != params.owner)
                return ValidationResult.error(
                    ValidationErrors.InvalidOwner(owner = params.owner, cpid = params.cpid)
                )

            if (auth.token != params.token)
                return ValidationResult.error(
                    ValidationErrors.InvalidToken(token = params.token, cpid = params.cpid)
                )
        }
        return ValidationResult.ok()
    }
}
