package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.*
import com.procurement.access.model.bpe.CommandMessage
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.lots.*
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.util.*

@Service
class LotsService(private val tenderProcessDao: TenderProcessDao) {

    fun getLots(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        return ResponseDto(data = GetLotsRs(
                awardCriteria = process.tender.awardCriteria!!.value,
                lots = getLotsDtoByStatus(process.tender.lots, LotStatus.ACTIVE))
        )
    }

    fun getLotsAuction(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        return ResponseDto(data = GetLotsAuctionRs(
                awardCriteria = process.tender.awardCriteria!!.value,
                tender = GetLotsAuctionTender(
                        id = process.tender.id!!,
                        title = process.tender.title,
                        description = process.tender.description,
                        awardCriteria = process.tender.awardCriteria!!.value,
                        lots = getLotsDtoByStatus(process.tender.lots, LotStatus.ACTIVE))))
    }

    fun setLotsStatusDetailsUnsuccessful(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val lotsDto = toObject(UpdateLotsRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.apply {
            setLotsStatusDetails(lots, lotsDto, LotStatusDetails.UNSUCCESSFUL)
        }
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ResponseDto(data = UpdateLotsRs(
                tenderStatus = process.tender.status,
                tenderStatusDetails = process.tender.statusDetails,
                lots = process.tender.lots,
                items = null))
    }

    fun setLotsStatusDetailsAwarded(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val dto = toObject(UpdateLotByBidRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        var statusDetails = if (dto.lotAwarded) {
            LotStatusDetails.AWARDED
        } else {
            LotStatusDetails.EMPTY
        }
        val updatedLot = setLotsStatusDetails(process.tender.lots, dto.lotId, statusDetails)
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ResponseDto(data = UpdateLotByBidRs(updatedLot))
    }

    fun setLotsStatusUnsuccessful(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val phase = cm.context.phase ?: throw ErrorException(CONTEXT)
        val lotsDto = toObject(UpdateLotsRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.apply {
            setLotsStatus(lots, lotsDto)
            if (!isAnyActiveLots(lots)) {
                status = TenderStatus.UNSUCCESSFUL
                statusDetails = TenderStatusDetails.EMPTY
            } else {
                statusDetails = TenderStatusDetails.fromValue(phase)
            }
        }
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ResponseDto(data = UpdateLotsRs(
                process.tender.status,
                process.tender.statusDetails,
                process.tender.lots,
                null))
    }

    fun setFinalStatuses(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val dto = toObject(FinalStatusesRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val lots = process.tender.lots

        var stageEnd = false
        var cpSuccess = false
        val lot = lots.asSequence().firstOrNull { it.id == dto.lotId }
        if (lot != null) {
            lot.status = LotStatus.UNSUCCESSFUL
            lot.statusDetails = LotStatusDetails.EMPTY
        }
//      if all lots have lot.status == "unsuccessful" || "cancelled"
//      tender.status == "unsuccessful" && tender.statusDetails == "empty"
//      stageEnd == TRUE; cpSuccess == FALSE
        if (lots.all { it.status == LotStatus.UNSUCCESSFUL || it.status == LotStatus.CANCELLED }) {
            process.tender.apply {
                status = TenderStatus.UNSUCCESSFUL
                statusDetails = TenderStatusDetails.EMPTY
            }
            stageEnd = true
            cpSuccess = false
//      if at least one lot with lot.status == "active"
//      stageEnd ==  FALSE; cpSuccess == TRUE
        } else if (lots.asSequence().any { it.status == LotStatus.ACTIVE }) {
            stageEnd = false
            cpSuccess = true
//      if at least one lot with lot.status == "complete" && all other lots have lot.status == "unsuccessful" || "cancelled"
//      tender.status == "complete" && tender.statusDetails == "empty"
//      stageEnd == TRUE; cpSuccess == TRUE
        } else {
            val completeLot = lots.asSequence().firstOrNull { it.status == LotStatus.COMPLETE }
            if (completeLot != null) {
                if (lots.asSequence().filter { it.id != completeLot.id }.all { it.status == LotStatus.UNSUCCESSFUL || it.status == LotStatus.CANCELLED }) {
                    process.tender.apply {
                        status = TenderStatus.COMPLETE
                        statusDetails = TenderStatusDetails.EMPTY
                    }
                    stageEnd = true
                    cpSuccess = true
                }
            }
        }
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        val lotsRs = lots.asSequence().map { FinalLot(id = it.id, status = it.status!!, statusDetails = it.statusDetails!!) }.toList()
        val tenderRs = FinalTender(id = process.tender.id!!, status = process.tender.status, statusDetails = process.tender.statusDetails)
        return ResponseDto(data = FinalStatusesRs(
                stageEnd = stageEnd,
                cpSuccess = cpSuccess,
                tender = tenderRs,
                lots = lotsRs))
    }

    fun setLotInitialStatus(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = "EV"
        val dto = toObject(CanCancellationRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val lot = process.tender.lots.first { it.id == dto.lotId }
        lot.apply {
            status = LotStatus.ACTIVE
            statusDetails = LotStatusDetails.EMPTY
        }
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ResponseDto(data = CanCancellationRs(
                lot = CanCancellationLot(
                        id = lot.id,
                        status = lot.status!!,
                        statusDetails = lot.statusDetails!!
                )
        ))
    }

    fun getAwardCriteria(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        return ResponseDto(data = GetAwardCriteriaRs(awardCriteria = process.tender.awardCriteria!!.value))
    }

    fun completeLots(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = "EV"
        val dto = toObject(ActivationAcRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)

        process.tender.lots.asSequence().filter { dto.relatedLots.contains(it.id) }
                .forEach {
                    it.status = LotStatus.COMPLETE
                    it.statusDetails = LotStatusDetails.EMPTY
                }
        val lotsRs = process.tender.lots.asSequence().map { ActivationAcLot(id = it.id, status = it.status!!, statusDetails = it.statusDetails!!) }.toList()
        val stageEnd = process.tender.lots.asSequence().none { it.status == LotStatus.ACTIVE }
        if (stageEnd) {
            process.tender.apply {
                status = TenderStatus.COMPLETE
                statusDetails = TenderStatusDetails.EMPTY
            }
        }
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ResponseDto(data = ActivationAcRs(
                tender = ActivationAcTender(
                        status = process.tender.status,
                        statusDetails = process.tender.statusDetails
                ),
                lots = lotsRs,
                stageEnd = stageEnd)
        )
    }

    private fun getLotsDtoByStatus(lots: List<Lot>, status: LotStatus): List<LotDto> {
        if (lots.isEmpty()) throw ErrorException(NO_ACTIVE_LOTS)
        val lotsByStatus = lots.asSequence()
                .filter { it.status == status }
                .map { LotDto(id = it.id, title = it.title, description = it.description, value = it.value) }.toList()
        if (lotsByStatus.isEmpty()) throw ErrorException(NO_ACTIVE_LOTS)
        return lotsByStatus
    }

    private fun setLotsStatus(lots: List<Lot>, updateLotsDto: UpdateLotsRq) {
        if (lots.isEmpty()) throw ErrorException(NO_ACTIVE_LOTS)
        val lotsIds = updateLotsDto.unsuccessfulLots?.asSequence()?.map { it.id }?.toHashSet() ?: HashSet()
        lots.forEach { lot ->
            if (lot.id in lotsIds) lot.status = LotStatus.UNSUCCESSFUL
            if (lot.statusDetails == LotStatusDetails.UNSUCCESSFUL) lot.statusDetails = LotStatusDetails.EMPTY
        }
    }

    private fun setLotsStatusDetails(lots: List<Lot>, updateLotsDto: UpdateLotsRq, statusDetails: LotStatusDetails) {
        if (lots.isEmpty()) throw ErrorException(NO_ACTIVE_LOTS)
        val lotsIds = updateLotsDto.unsuccessfulLots?.asSequence()?.map { it.id }?.toHashSet() ?: HashSet()
        lots.forEach { lot ->
            if (lot.id in lotsIds) lot.statusDetails = statusDetails
        }
    }

    private fun isAnyActiveLots(lots: List<Lot>): Boolean {
        return lots.asSequence().any { it.status == LotStatus.ACTIVE && it.statusDetails == LotStatusDetails.EMPTY }
    }

    private fun setLotsStatusDetails(lots: List<Lot>, lotId: String, lotStatusDetails: LotStatusDetails): Lot {
        return lots.asSequence()
                .filter { it.id == lotId }
                .first()
                .apply { statusDetails = lotStatusDetails }
    }
}
