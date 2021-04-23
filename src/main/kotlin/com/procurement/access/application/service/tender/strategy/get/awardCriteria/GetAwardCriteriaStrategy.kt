package com.procurement.access.application.service.tender.strategy.get.awardCriteria

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.RfqEntity
import com.procurement.access.utils.toObject

class GetAwardCriteriaStrategy(
    private val tenderProcessDao: TenderProcessDao
) {
    fun execute(context: GetAwardCriteriaContext): GetAwardCriteriaResult {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId = context.cpid, stage = context.stage.key)
            ?: throw ErrorException(DATA_NOT_FOUND)

        return when (context.stage) {
            Stage.AC,
            Stage.EV,
            Stage.FE,
            Stage.NP,
            Stage.TP -> {
                val cn = toObject(CNEntity::class.java, entity.jsonData)
                GetAwardCriteriaResult(awardCriteria = cn.tender.awardCriteria!!)
            }

            Stage.RQ -> {
                val rq = toObject(RfqEntity::class.java, entity.jsonData)
                GetAwardCriteriaResult(awardCriteria = rq.tender.awardCriteria)
            }

            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PN -> throw ErrorException(
                error = ErrorType.INVALID_STAGE,
                message = "Stage ${context.stage} not allowed at the command."
            )
        }
    }
}
