package com.procurement.access.service

import com.procurement.access.application.model.context.CheckExistanceItemsAndLotsContext
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

interface ApValidationService {
    fun checkExistanceItemsAndLots(context: CheckExistanceItemsAndLotsContext)
}

@Service
class ApValidationServiceImpl(private val tenderProcessDao: TenderProcessDao) : ApValidationService {

    override fun checkExistanceItemsAndLots(context: CheckExistanceItemsAndLotsContext) {
        val cpid = context.cpid
        val stage = context.stage

        val apEntity: APEntity = tenderProcessDao.getByCpIdAndStage(cpId = cpid, stage = stage)
            ?.let { apEntity -> toObject(APEntity::class.java, apEntity.jsonData) }
            ?: throw ErrorException( // VR.COM-1.26.1
                error = ErrorType.ENTITY_NOT_FOUND,
                message = "Cannot found tender by cpid='${cpid}' and stage='${stage}'."
            )

        // VR.COM-1.26.2
        if (apEntity.tender.items == null || apEntity.tender.items.isEmpty())
            throw ErrorException(
                error = ErrorType.DATA_NOT_FOUND,
                message = "Cannot found items in tender (cpid='${cpid}' and stage='${stage}')"
            )

        // VR.COM-1.26.3
        if (apEntity.tender.lots == null || apEntity.tender.lots.isEmpty())
            throw ErrorException(
                error = ErrorType.DATA_NOT_FOUND,
                message = "Cannot found lots in tender (cpid='${cpid}' and stage='${stage}')"
            )
    }
}
