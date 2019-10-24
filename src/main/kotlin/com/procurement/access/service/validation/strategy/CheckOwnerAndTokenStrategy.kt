package com.procurement.access.service.validation.strategy

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.cpid
import com.procurement.access.model.dto.bpe.owner
import com.procurement.access.model.dto.bpe.token

class CheckOwnerAndTokenStrategy(private val tenderProcessDao: TenderProcessDao) {

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
}
