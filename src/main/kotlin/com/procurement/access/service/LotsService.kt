package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.lots.*
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.util.*

interface LotsService {

    fun getLots(cpId: String, stage: String, status: TenderStatus): ResponseDto

    fun updateStatus(cpId: String, stage: String, tenderStatus: TenderStatus, lotsDto: LotsRequestDto): ResponseDto

    fun updateStatusDetails(cpId: String, stage: String, tenderStatusDetails: TenderStatusDetails, lotsDto: LotsRequestDto): ResponseDto

    fun updateStatusDetailsById(cpId: String, stage: String, lotId: String, statusDetails: TenderStatusDetails): ResponseDto

    fun checkStatusDetails(cpId: String, stage: String): ResponseDto

    fun updateLots(cpId: String, stage: String, lotsDto: LotsRequestDto): ResponseDto

}

@Service
class LotsServiceImpl(private val tenderProcessDao: TenderProcessDao) : LotsService {

    override fun getLots(cpId: String,
                         stage: String,
                         status: TenderStatus): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val lotsResponseDto = LotsResponseDto(
                process.tender.awardCriteria.value(),
                getLotsDtoByStatus(process.tender.lots, status))
        return ResponseDto(true, null, lotsResponseDto)
    }

    override fun updateStatus(cpId: String,
                              stage: String,
                              tenderStatus: TenderStatus,
                              lotsDto: LotsRequestDto): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.apply {
            setLotsStatus(lots, lotsDto, tenderStatus)
            if (!isAnyActiveLots(lots)) {
                status = TenderStatus.UNSUCCESSFUL
                statusDetails = TenderStatusDetails.EMPTY
            }
        }
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ResponseDto(true, null,
                LotsUpdateResponseDto(process.tender.status, process.tender.lots, null))
    }

    override fun updateStatusDetails(cpId: String,
                                     stage: String,
                                     tenderStatusDetails: TenderStatusDetails,
                                     lotsDto: LotsRequestDto): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.apply {
            setLotsStatusDetails(lots, lotsDto, tenderStatusDetails)
        }
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ResponseDto(true, null,
                LotsUpdateResponseDto(process.tender.status, process.tender.lots, null))
    }

    override fun updateStatusDetailsById(cpId: String,
                                         stage: String,
                                         lotId: String,
                                         statusDetails: TenderStatusDetails): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val updatedLot = setLotsStatusDetails(process.tender.lots, lotId, statusDetails)
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ResponseDto(true, null, LotUpdateResponseDto(updatedLot))
    }

    override fun checkStatusDetails(cpId: String,
                                    stage: String): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        checkLotStatusDetails(process.tender.lots)
        return ResponseDto(true, null, "All active lots are awarded.")
    }

    override fun updateLots(cpId: String,
                            stage: String,
                            lotsDto: LotsRequestDto): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        var itemsForCompiledLots: List<Item>? = null
        val updatedLots = updateLots(process.tender.lots, lotsDto)
        process.tender.apply {
            if (isAnyCompleteLots(updatedLots)) {
                itemsForCompiledLots = getItemsForCompiledLots(items, updatedLots)
            } else {
                status = TenderStatus.UNSUCCESSFUL
                statusDetails = TenderStatusDetails.EMPTY
            }
        }
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ResponseDto(true, null,
                LotsUpdateResponseDto(process.tender.status, updatedLots, itemsForCompiledLots))
    }

    private fun getLotsDtoByStatus(lots: List<Lot>, status: TenderStatus): List<LotDto> {
        if (lots.isEmpty()) throw ErrorException(ErrorType.NO_ACTIVE_LOTS)
        val lotsByStatus = lots.asSequence()
                .filter { it.status == status }
                .map { LotDto(it.id) }.toList()
        if (lotsByStatus.isEmpty()) throw ErrorException(ErrorType.NO_ACTIVE_LOTS)
        return lotsByStatus
    }

    private fun setLotsStatus(lots: List<Lot>, lotsDto: LotsRequestDto, status: TenderStatus) {
        if (lots.isEmpty()) throw ErrorException(ErrorType.NO_ACTIVE_LOTS)
        val lotsIds = lotsDto.unsuccessfulLots?.asSequence()?.map { it.id }?.toHashSet() ?: HashSet()
        lots.forEach { lot ->
            if (lot.id in lotsIds) lot.status = status
            if (lot.statusDetails == TenderStatusDetails.UNSUCCESSFUL) lot.statusDetails = TenderStatusDetails.EMPTY
        }
    }

    private fun setLotsStatusDetails(lots: List<Lot>, lotsDto: LotsRequestDto, statusDetails: TenderStatusDetails) {
        if (lots.isEmpty()) throw ErrorException(ErrorType.NO_ACTIVE_LOTS)
        val lotsIds = lotsDto.unsuccessfulLots?.asSequence()?.map { it.id }?.toHashSet() ?: HashSet()
        lots.forEach { lot ->
            if (lot.id in lotsIds) lot.statusDetails = statusDetails
        }
    }

    private fun isAnyActiveLots(lots: List<Lot>?): Boolean {
        return lots?.asSequence()
                ?.any { it.status == TenderStatus.ACTIVE && it.statusDetails == TenderStatusDetails.EMPTY }
                ?: false
    }

    private fun setLotsStatusDetails(lots: List<Lot>?, lotId: String, lotStatusDetails: TenderStatusDetails): Lot {
        return lots?.asSequence()
                ?.filter({ it.id == lotId })
                ?.first()
                ?.apply { statusDetails = lotStatusDetails }
                ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
    }

    private fun checkLotStatusDetails(lots: List<Lot>) {
        val predicate = { lot: Lot -> lot.status == TenderStatus.ACTIVE && lot.statusDetails != TenderStatusDetails.AWARDED }
        if (lots.asSequence().any(predicate)) throw ErrorException(ErrorType.NOT_ALL_LOTS_AWARDED)
    }

    private fun getItemsForCompiledLots(items: List<Item>?, lots: List<Lot>): List<Item>? {
        if (items != null) {
            val lotsIds = lots.asSequence()
                    .filter { it.status == TenderStatus.COMPLETE && it.statusDetails == TenderStatusDetails.EMPTY }
                    .map { it.id }.toHashSet()
            return items.asSequence().filter { lotsIds.contains(it.relatedLot) }.toList()
        }
        return null
    }

    private fun updateLots(lots: List<Lot>, unsuccessfulLots: LotsRequestDto): List<Lot> {
        if (lots.isEmpty()) throw ErrorException(ErrorType.NO_ACTIVE_LOTS)
        val lotIds = unsuccessfulLots.unsuccessfulLots?.asSequence()?.map { it.id }?.toHashSet() ?: HashSet()
        lots.forEach { lot ->
            if (lot.status == TenderStatus.ACTIVE && lot.statusDetails == TenderStatusDetails.AWARDED) {
                lot.status = TenderStatus.COMPLETE
                lot.statusDetails = TenderStatusDetails.EMPTY
            }
            if (lotIds.contains(lot.id)) {
                lot.status = TenderStatus.UNSUCCESSFUL
                lot.statusDetails = TenderStatusDetails.EMPTY
            }
        }
        return lots
    }

    private fun isAnyCompleteLots(lots: List<Lot>?): Boolean {
        return if (lots != null && !lots.isEmpty()) {
            lots.asSequence()
                    .any { it.status == TenderStatus.COMPLETE && it.statusDetails == TenderStatusDetails.EMPTY }
        } else false
    }
}
