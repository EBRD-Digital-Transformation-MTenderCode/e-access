package com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.RfqEntity
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject

class SetTenderUnsuccessfulStrategy(
    private val tenderProcessDao: TenderProcessDao
) {
    fun execute(context: SetTenderUnsuccessfulContext): SetTenderUnsuccessfulResult {
        val entity = tenderProcessDao.getByCpidAndOcid(cpid = context.cpid, ocid = context.ocid)
            ?: throw ErrorException(DATA_NOT_FOUND)

        val (tenderJson, result) = when (context.stage) {
            Stage.AC,
            Stage.EV,
            Stage.FE,
            Stage.NP,
            Stage.TP -> {
                val tenderProcess = toObject(CNEntity::class.java, entity.jsonData)
                val unsuccessfulLotsIds = mutableListOf<String>()
                val updatedLots = tenderProcess.tender.lots.map { lot ->
                    if (lot.status == LotStatus.ACTIVE)
                        lot
                            .copy(status = LotStatus.UNSUCCESSFUL, statusDetails = LotStatusDetails.EMPTY)
                            .also { unsuccessfulLot -> unsuccessfulLotsIds.add(unsuccessfulLot.id) }
                    else
                        lot
                }

                val updatedTenderProcess = tenderProcess.copy(
                    tender = tenderProcess.tender.copy(
                        status = TenderStatus.UNSUCCESSFUL,
                        statusDetails = TenderStatusDetails.EMPTY,
                        lots = updatedLots
                    )
                )

                val tenderForResponse = updatedTenderProcess.copy(
                    tender = updatedTenderProcess.tender.copy(
                        lots = updatedTenderProcess.tender.lots.filter { it.id in unsuccessfulLotsIds }
                    )
                )

                val result = SetTenderUnsuccessfulResult.fromDomain(tenderForResponse)

                toJson(updatedTenderProcess) to result
            }

            Stage.RQ -> {
                val rq = toObject(RfqEntity::class.java, entity.jsonData)
                val unsuccessfulLotsIds = mutableListOf<LotId>()
                val updatedLots = rq.tender.lots.map { lot ->
                    if (lot.status == LotStatus.ACTIVE)
                        lot
                            .copy(status = LotStatus.UNSUCCESSFUL, statusDetails = LotStatusDetails.EMPTY)
                            .also { unsuccessfulLot -> unsuccessfulLotsIds.add(unsuccessfulLot.id) }
                    else
                        lot
                }

                val updatedRq = rq.copy(
                    tender = rq.tender.copy(
                        status = TenderStatus.UNSUCCESSFUL,
                        statusDetails = TenderStatusDetails.EMPTY,
                        lots = updatedLots
                    )
                )

                val tenderForResponse = updatedRq.copy(
                    tender = updatedRq.tender.copy(
                        lots = updatedRq.tender.lots.filter { it.id in unsuccessfulLotsIds }
                    )
                )

                val result = SetTenderUnsuccessfulResult.fromDomain(tenderForResponse)

                toJson(updatedRq) to result
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

        tenderProcessDao.save(
            TenderProcessEntity(
                cpId = context.cpid,
                token = entity.token,
                ocid = context.ocid,
                owner = entity.owner,
                createdDate = context.startDate,
                jsonData = tenderJson
            )
        )

        return result
    }
}
