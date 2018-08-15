package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.TenderStatusResponseDto
import com.procurement.access.model.dto.lots.LotsUpdateResponseDto
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

    fun setSuspended(cpId: String, stage: String, suspended: Boolean?): ResponseDto

    fun setUnsuccessful(cpId: String, stage: String): ResponseDto

    fun checkToken(cpId: String, stage: String, token: String): ResponseDto
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
                              suspended: Boolean?): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        if (suspended!!) {
            process.tender.statusDetails = TenderStatusDetails.SUSPENDED
        } else {
            process.tender.statusDetails = TenderStatusDetails.EMPTY
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

    override fun checkToken(cpId: String, stage: String, token: String): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        return ResponseDto(data = "ok")
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
