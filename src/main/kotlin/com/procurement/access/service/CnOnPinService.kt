package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.CONTEXT
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.exception.ErrorType.INVALID_DOCS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_OWNER
import com.procurement.access.exception.ErrorType.INVALID_START_DATE
import com.procurement.access.exception.ErrorType.INVALID_TOKEN
import com.procurement.access.infrastructure.api.v1.ApiResponseV1
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.commandId
import com.procurement.access.infrastructure.api.v1.startDate
import com.procurement.access.infrastructure.handler.v1.model.request.CnUpdate
import com.procurement.access.infrastructure.handler.v1.model.request.TenderCnUpdate
import com.procurement.access.infrastructure.handler.v1.model.request.validate
import com.procurement.access.lib.extension.toSet
import com.procurement.access.model.dto.ocds.Tender
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CnOnPinService(private val tenderProcessDao: TenderProcessDao) {

    fun createCnOnPin(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val token = cm.context.token ?: throw ErrorException(CONTEXT)
        val previousStage = cm.context.prevStage ?: throw ErrorException(CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
        val dateTime = cm.startDate
        val cnDto = toObject(CnUpdate::class.java, cm.data).validate()

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, previousStage) ?: throw ErrorException(DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(INVALID_TOKEN)
        val tenderProcess = toObject(TenderProcess::class.java, entity.jsonData)
        validatePeriod(tenderProcess.tender, dateTime)
        validateDocumentsRelatedLots(tenderProcess.tender, cnDto.tender)
        tenderProcess.tender.documents = cnDto.tender.documents
        tenderProcess.tender.tenderPeriod = null
        setStatuses(tenderProcess.tender)
        tenderProcessDao.save(getEntity(tenderProcess, entity, dateTime))
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = tenderProcess)
    }

    private fun validatePeriod(pinTender: Tender, dateTime: LocalDateTime) {
        val tenderPeriod = pinTender.tenderPeriod ?: throw ErrorException(INVALID_START_DATE)
        if (tenderPeriod.startDate.toLocalDate() != dateTime.toLocalDate())
            throw ErrorException(INVALID_START_DATE)
    }

    private fun validateDocumentsRelatedLots(tender: Tender, tenderDto: TenderCnUpdate) {
        val lotsFromPin = tender.lots.toSet { it.id }
        val lotsFromDocuments = tenderDto.documents
            .asSequence()
            .filter { it.relatedLots != null }
            .flatMap { it.relatedLots!!.asSequence() }
            .toSet()
        if (lotsFromDocuments.isNotEmpty()) {
            if (lotsFromDocuments.size > lotsFromPin.size) throw ErrorException(INVALID_DOCS_RELATED_LOTS)
            if (!lotsFromPin.containsAll(lotsFromDocuments)) throw ErrorException(INVALID_DOCS_RELATED_LOTS)
        }
    }

    private fun setStatuses(tender: Tender) {
        tender.status = TenderStatus.ACTIVE
        tender.statusDetails = TenderStatusDetails.EMPTY
        tender.lots.forEach { lot ->
            lot.status = LotStatus.ACTIVE
            lot.statusDetails = LotStatusDetails.EMPTY
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
            createdDate = dateTime,
            jsonData = toJson(tp)
        )
    }
}
