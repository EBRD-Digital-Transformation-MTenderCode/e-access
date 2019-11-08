package com.procurement.access.application.service.tender.strategy.get.awardCriteria

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.utils.toObject

class GetAwardCriteriaStrategy(
    private val tenderProcessDao: TenderProcessDao
) {
    fun execute(context: GetAwardCriteriaContext): GetAwardCriteriaResult {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId = context.cpid, stage = context.stage)
            ?: throw ErrorException(DATA_NOT_FOUND)
        val cn: CNEntity = toObject(CNEntity::class.java, entity.jsonData)
        return GetAwardCriteriaResult(
            awardCriteria = cn.tender.awardCriteria!!
        )
    }
}
