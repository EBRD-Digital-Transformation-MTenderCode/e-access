package com.procurement.access.application.service.tender.strategy.get.awardCriteria

import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.RfqEntity
import com.procurement.access.infrastructure.repository.CassandraTenderProcessRepositoryV1
import com.procurement.access.utils.toObject

class GetAwardCriteriaStrategy(
    private val tenderRepository: CassandraTenderProcessRepositoryV1
) {
    fun execute(context: GetAwardCriteriaContext): GetAwardCriteriaResult {
        val entity = tenderRepository.getByCpidAndOcid(cpid = context.cpid, ocid = context.ocid)
            ?: throw ErrorException(DATA_NOT_FOUND)

        return when (context.ocid.stage) {
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
            Stage.PN,
            Stage.PO -> throw ErrorException(
                error = ErrorType.INVALID_STAGE,
                message = "Stage ${context.ocid.stage} not allowed at the command."
            )
        }
    }
}
