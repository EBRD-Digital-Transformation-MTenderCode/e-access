package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.bids.CheckBidRQDto
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

interface BidsService {

    fun checkBid(cpId: String, stage: String, bid: CheckBidRQDto): ResponseDto

}

@Service
class BidsServiceImpl(private val tenderProcessDao: TenderProcessDao) : BidsService {

    override fun checkBid(cpId: String, stage: String, bid: CheckBidRQDto): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        if (bid.bidDto.value.currency != process.tender.value.currency) throw ErrorException(ErrorType.INVALID_CURRENCY)
        for (lot in process.tender.lots) {
            if (bid.bidDto.relatedLot.contains(lot.id)) {
                if (bid.bidDto.value.amount > lot.value.amount) throw ErrorException(ErrorType.BID_VALUE_MORE_THAN_SUM_LOTS)
                if (!(lot.status == TenderStatus.ACTIVE && lot.statusDetails == TenderStatusDetails.EMPTY)) throw ErrorException(ErrorType.BID_VALUE_MORE_THAN_SUM_LOTS)
            } else {
                throw ErrorException(ErrorType.BID_VALUE_MORE_THAN_SUM_LOTS)
            }
        }
        return ResponseDto(true, null, null)
    }
}
