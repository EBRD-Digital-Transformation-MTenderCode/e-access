package com.procurement.access.service.validation.strategy

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.entity.TenderProcessEntity

class CheckOwnerAndTokenStrategy(private val tenderProcessDao: TenderProcessDao) {

    fun checkOwnerAndToken(cm: CommandMessage) {
        val cpId = getCPID(cm)
        val stage = getStage(cm)
        val token = getToken(cm)
        val owner = getOwner(cm)
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)

        //VR-3.12.3
        checkOwner(ownerFromRequest = owner, entity = entity)

        //VR-3.12.4
        checkToken(tokenFromRequest = token, entity = entity)
    }

    /**
     * VR-3.12.3 owner
     *
     * eAccess проверяет соответствие owner предложения в БД и полученного platformId.
     */
    private fun checkOwner(ownerFromRequest: String, entity: TenderProcessEntity) {
        if (entity.owner != ownerFromRequest)
            throw ErrorException(error = ErrorType.INVALID_OWNER)
    }

    /**
     * VR-3.12.4 token
     *
     * eAccess проверяет что найденный по token из запароса тендер содержит tender.ID,
     * значение которого равно занчению параметра cpid из запроса.
     */
    private fun checkToken(tokenFromRequest: String, entity: TenderProcessEntity) {
        if (entity.token.toString() != tokenFromRequest)
            throw ErrorException(error = ErrorType.INVALID_TOKEN)
    }

    private fun getCPID(cm: CommandMessage): String {
        return cm.context.cpid
            ?: throw ErrorException(
                error = ErrorType.CONTEXT,
                message = "Missing the 'cpid' attribute in context."
            )
    }

    private fun getStage(cm: CommandMessage): String {
        return cm.context.stage
            ?: throw ErrorException(
                error = ErrorType.CONTEXT,
                message = "Missing the 'stage' attribute in context."
            )
    }

    private fun getToken(cm: CommandMessage): String {
        return cm.context.token
            ?: throw ErrorException(
                error = ErrorType.CONTEXT,
                message = "Missing the 'token' attribute in context."
            )
    }

    private fun getOwner(cm: CommandMessage): String {
        return cm.context.owner
            ?: throw ErrorException(
                error = ErrorType.CONTEXT,
                message = "Missing the 'owner' attribute in context."
            )
    }
}
