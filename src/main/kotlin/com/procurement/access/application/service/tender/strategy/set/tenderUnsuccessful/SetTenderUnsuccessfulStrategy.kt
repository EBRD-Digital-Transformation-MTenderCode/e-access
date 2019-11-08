package com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject

class SetTenderUnsuccessfulStrategy(
    private val tenderProcessDao: TenderProcessDao
) {
    fun execute(context: SetTenderUnsuccessfulContext): SetTenderUnsuccessfulResult {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId = context.cpid, stage = context.stage)
            ?: throw ErrorException(DATA_NOT_FOUND)

        val cn = toObject(CNEntity::class.java, entity.jsonData)

        val idsActiveLots = cn.tender.lots.asSequence()
            .filter { lot -> lot.status == LotStatus.ACTIVE }
            .map { lot -> lot.id }
            .toSet()

        val updatedLots = cn.tender.lots.setStatusUnsuccessful(idsActiveLots)

        val updatedCN = cn.copy(
            tender = cn.tender.copy(
                status = TenderStatus.UNSUCCESSFUL,
                statusDetails = TenderStatusDetails.EMPTY,
                lots = updatedLots
            )
        )

        tenderProcessDao.save(
            TenderProcessEntity(
                cpId = context.cpid,
                token = entity.token,
                stage = context.stage,
                owner = entity.owner,
                createdDate = context.startDate.toDate(),
                jsonData = toJson(updatedCN)
            )
        )

        return SetTenderUnsuccessfulResult(
            tender = SetTenderUnsuccessfulResult.Tender(
                status = updatedCN.tender.status,
                statusDetails = updatedCN.tender.statusDetails
            ),
            lots = updatedCN.tender.lots.asSequence()
                .filter { lot ->
                    lot.id in idsActiveLots
                }
                .map { lot ->

                    SetTenderUnsuccessfulResult.Lot(
                        id = LotId.fromString(lot.id),
                        status = lot.status
                    )
                }
                .toList()
        )
    }

    private fun List<CNEntity.Tender.Lot>.setStatusUnsuccessful(ids: Set<String>) = this.map { lot ->
        if (lot.id in ids)
            lot.copy(
                status = LotStatus.UNSUCCESSFUL,
                statusDetails = LotStatusDetails.EMPTY
            )
        else
            lot
    }
}
