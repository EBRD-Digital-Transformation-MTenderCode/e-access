package com.procurement.access.application.service.lot

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.LotStatus
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

interface LotService {
    fun getLotsForAuction(context: LotsForAuctionContext, data: LotsForAuctionData): LotsForAuction
}

@Service
class LotServiceImpl(
    private val tenderProcessDao: TenderProcessDao
) : LotService {
    override fun getLotsForAuction(context: LotsForAuctionContext, data: LotsForAuctionData): LotsForAuction {
        return tenderProcessDao.getByCpIdAndStage(context.cpid, context.stage)
            ?.let { entity ->
                val process = toObject(TenderProcess::class.java, entity.jsonData)
                getLotFromTender(lots = process.tender.lots)
            }
            ?: getLotFromRequest(lots = data.lots)
    }

    private fun getLotFromRequest(lots: List<LotsForAuctionData.Lot>): LotsForAuction = LotsForAuction(
        lots = lots.map { lot ->
            LotsForAuction.Lot(
                id = lot.id,
                value = lot.value.let { value ->
                    LotsForAuction.Lot.Value(
                        amount = value.amount,
                        currency = value.currency
                    )
                }
            )
        }
    )

    private fun getLotFromTender(lots: List<Lot>): LotsForAuction = LotsForAuction(
        lots = lots.asSequence()
            .filter { it.status == LotStatus.PLANNED }
            .map { lot ->
                LotsForAuction.Lot(
                    id = lot.id,
                    value = lot.value.let { value ->
                        LotsForAuction.Lot.Value(
                            amount = value.amount,
                            currency = value.currency
                        )
                    }
                )
            }
            .toList()
    )
}