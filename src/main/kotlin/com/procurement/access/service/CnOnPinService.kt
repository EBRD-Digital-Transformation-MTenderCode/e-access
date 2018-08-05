package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.CnCreate
import com.procurement.access.model.dto.cn.CnUpdate
import com.procurement.access.model.dto.cn.TenderCnUpdate
import com.procurement.access.model.dto.ocds.Tender
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface CnOnPinService {

    fun createCnOnPin(
            cpId: String,
            previousStage: String,
            stage: String,
            owner: String,
            token: String,
            dateTime: LocalDateTime,
            cnDto: CnUpdate): ResponseDto
}

@Service
class CnOnPinServiceImpl(private val tenderProcessDao: TenderProcessDao) : CnOnPinService {

    override fun createCnOnPin(cpId: String,
                               previousStage: String,
                               stage: String,
                               owner: String,
                               token: String,
                               dateTime: LocalDateTime,
                               cnDto: CnUpdate): ResponseDto {

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, previousStage)
                ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        val tenderProcess = toObject(TenderProcess::class.java, entity.jsonData)
        validatePeriod(tenderProcess.tender, dateTime)
        validateDocumentsRelatedLots(tenderProcess.tender, cnDto.tender)
        tenderProcess.tender.documents = cnDto.tender.documents
        setStatuses(tenderProcess.tender)
        tenderProcessDao.save(getEntity(tenderProcess, entity, dateTime))
        return ResponseDto(data = tenderProcess)
    }

    private fun validatePeriod(pinTender: Tender, dateTime: LocalDateTime) {
        if (pinTender.tenderPeriod.startDate.toLocalDate() != dateTime.toLocalDate())
            throw ErrorException(ErrorType.INVALID_START_DATE)
    }

    private fun validateDocumentsRelatedLots(tender: Tender, tenderDto: TenderCnUpdate) {
        val lotsFromPin = tender.lots.asSequence().map { it.id }.toHashSet()
        val lotsFromDocuments = tenderDto.documents.asSequence()
                .filter { it.relatedLots != null }
                .flatMap { it.relatedLots!!.asSequence() }.toHashSet()
        if (lotsFromDocuments.isNotEmpty()) {
            if (lotsFromDocuments.size > lotsFromPin.size) throw ErrorException(ErrorType.INVALID_DOCS_RELATED_LOTS)
            if (!lotsFromPin.containsAll(lotsFromDocuments)) throw ErrorException(ErrorType.INVALID_DOCS_RELATED_LOTS)
        }
    }

    private fun setStatuses(tender: Tender) {
        tender.status = TenderStatus.ACTIVE
        tender.statusDetails = TenderStatusDetails.EMPTY
        tender.lots.forEach { lot ->
            lot.status = TenderStatus.ACTIVE
            lot.statusDetails = TenderStatusDetails.EMPTY
        }
    }

    private fun getEntity(tp: TenderProcess,
                          entity: TenderProcessEntity,
                          dateTime: LocalDateTime): TenderProcessEntity {
        return TenderProcessEntity(
                cpId = entity.cpId,
                token = entity.token,
                stage = entity.stage,
                owner = entity.owner,
                createdDate = dateTime.toDate(),
                jsonData = toJson(tp)
        )
    }
}
