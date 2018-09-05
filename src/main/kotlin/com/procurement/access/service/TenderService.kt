package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.TenderStatusResponseDto
import com.procurement.access.model.dto.lots.CancellationResponseDto
import com.procurement.access.model.dto.lots.LotCancellation
import com.procurement.access.model.dto.lots.LotsUpdateResponseDto
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.localNowUTC
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

interface TenderService {

    fun updateStatus(cpId: String, stage: String, status: TenderStatus): ResponseDto

    fun updateStatusDetails(cpId: String, stage: String, statusDetails: TenderStatusDetails): ResponseDto

    fun setSuspended(cpId: String, stage: String, suspended: Boolean): ResponseDto

    fun setUnsuccessful(cpId: String, stage: String): ResponseDto

    fun prepareCancellation(cpId: String, stage: String, owner: String, token: String, operationType: String): ResponseDto

    fun tenderCancellation(cpId: String, stage: String, owner: String, token: String, operationType: String): ResponseDto
}

@Service
class TenderServiceImpl(private val tenderProcessDao: TenderProcessDao) : TenderService {

    override fun updateStatus(cpId: String,
                              stage: String,
                              status: TenderStatus): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.status = status
        tenderProcessDao.save(getEntity(process, entity))
        return ResponseDto(data = TenderStatusResponseDto(process.tender.status.value(), process.tender.statusDetails.value()))
    }

    override fun updateStatusDetails(cpId: String,
                                     stage: String,
                                     statusDetails: TenderStatusDetails): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.statusDetails = statusDetails
        tenderProcessDao.save(getEntity(process, entity))
        return ResponseDto(data = TenderStatusResponseDto(process.tender.status.value(), process.tender.statusDetails.value()))
    }

    override fun setSuspended(cpId: String,
                              stage: String,
                              suspended: Boolean): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        if (suspended) {
            process.tender.statusDetails = TenderStatusDetails.SUSPENDED
        } else {
            if (process.tender.statusDetails == TenderStatusDetails.SUSPENDED) {
                process.tender.statusDetails = TenderStatusDetails.EMPTY
            } else {
                return ResponseDto(data = TenderStatusResponseDto(null, null))
            }
        }
        tenderProcessDao.save(getEntity(process, entity))
        return ResponseDto(data = TenderStatusResponseDto(process.tender.status.value(), process.tender.statusDetails.value()))
    }

    override fun setUnsuccessful(cpId: String,
                                 stage: String): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.apply {
            status = TenderStatus.UNSUCCESSFUL
            statusDetails = TenderStatusDetails.EMPTY
            lots.forEach { lot ->
                lot.status = TenderStatus.UNSUCCESSFUL
                lot.statusDetails = TenderStatusDetails.EMPTY

            }
        }
        tenderProcessDao.save(getEntity(process, entity))
        return ResponseDto(data = LotsUpdateResponseDto(process.tender.status, process.tender.lots, null))
    }

    override fun prepareCancellation(cpId: String, stage: String, owner: String, token: String, operationType: String): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        validateTenderStatusForPrepareCancellation(process, operationType)
        val lotStatusPredicate = getLotStatusPredicateForPrepareCancellation(operationType)
        val lotsResponseDto = mutableListOf<LotCancellation>()
        process.tender.apply {
            statusDetails = TenderStatusDetails.CANCELLED
            lots.asSequence()
                    .filter(lotStatusPredicate)
                    .forEach { lot ->
                        lot.statusDetails = TenderStatusDetails.CANCELLED
                        addLotToLotsResponseDto(lotsResponseDto, lot)
                    }
        }
        tenderProcessDao.save(getEntity(process, entity))
        return ResponseDto(data = CancellationResponseDto(lots = lotsResponseDto))
    }


    override fun tenderCancellation(cpId: String, stage: String, owner: String, token: String, operationType: String): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        validateTenderStatusForCancellation(process, operationType)
        val lotStatusPredicate = getLotStatusPredicateForCancellation(operationType)
        val lotsResponseDto = mutableListOf<LotCancellation>()
        process.tender.apply {
            status = TenderStatus.CANCELLED
            lots.asSequence()
                    .filter(lotStatusPredicate)
                    .forEach { lot ->
                        lot.status = TenderStatus.CANCELLED
                        lot.statusDetails = TenderStatusDetails.EMPTY
                        addLotToLotsResponseDto(lotsResponseDto, lot)
                    }
        }
        tenderProcessDao.save(getEntity(process, entity))
        return ResponseDto(data = CancellationResponseDto(lots = lotsResponseDto))
    }

    private fun getLotStatusPredicateForPrepareCancellation(operationType: String): (Lot) -> Boolean {
        return when (operationType) {
            "cancelTender", "cancelTenderEv" -> { lot: Lot ->
                (lot.status == TenderStatus.ACTIVE)
                        && (lot.statusDetails == TenderStatusDetails.EMPTY
                        || lot.statusDetails == TenderStatusDetails.AWARDED)
            }
            "cancelPlan" -> { lot: Lot ->
                (lot.status == TenderStatus.PLANNING
                        || lot.status == TenderStatus.PLANNED)
                        && (lot.statusDetails == TenderStatusDetails.EMPTY)
            }
            else -> {
                throw ErrorException(ErrorType.INVALID_OPERATION_TYPE)
            }
        }
    }

    private fun getLotStatusPredicateForCancellation(operationType: String): (Lot) -> Boolean {
        return when (operationType) {
            "cancelTender", "cancelTenderEv" -> { lot: Lot ->
                (lot.status == TenderStatus.ACTIVE)
                        && (lot.statusDetails == TenderStatusDetails.CANCELLED)
            }
            "cancelPlan" -> { lot: Lot ->
                (lot.status == TenderStatus.PLANNING || lot.status == TenderStatus.PLANNED)
                        && (lot.statusDetails == TenderStatusDetails.EMPTY)
            }
            else -> {
                throw ErrorException(ErrorType.INVALID_OPERATION_TYPE)
            }
        }
    }

    private fun validateTenderStatusForPrepareCancellation(process: TenderProcess, operationType: String) {
        when (operationType) {
            "cancelTender", "cancelTenderEv" -> {
                if (process.tender.status != TenderStatus.ACTIVE)
                    throw ErrorException(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS)
                if (process.tender.statusDetails != TenderStatusDetails.EMPTY)
                    throw ErrorException(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS)
            }
        }
    }

    private fun validateTenderStatusForCancellation(process: TenderProcess, operationType: String) {
        when (operationType) {
            "cancelTender", "cancelTenderEv" -> {
                if (process.tender.status != TenderStatus.ACTIVE)
                    throw ErrorException(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS)
                if (process.tender.statusDetails != TenderStatusDetails.CANCELLED)
                    throw ErrorException(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS)
            }
            "cancelPlan" -> {
                if (process.tender.status != TenderStatus.PLANNING && process.tender.status != TenderStatus.PLANNED)
                    throw ErrorException(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS)
                if (process.tender.statusDetails != TenderStatusDetails.EMPTY)
                    throw ErrorException(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS)

            }
        }
    }

    private fun addLotToLotsResponseDto(lotsResponseDto: MutableList<LotCancellation>, lot: Lot) {
        lotsResponseDto.add(LotCancellation(
                id = lot.id,
                status = lot.status,
                statusDetails = lot.statusDetails))
    }

    private fun getEntity(process: TenderProcess,
                          entity: TenderProcessEntity): TenderProcessEntity {

        return TenderProcessEntity(
                cpId = entity.cpId,
                token = entity.token,
                stage = entity.stage,
                owner = entity.owner,
                createdDate = localNowUTC().toDate(),
                jsonData = toJson(process)
        )
    }
}
