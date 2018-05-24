package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.model.dto.ocds.TenderStatus.PLANNED
import com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY
import com.procurement.access.model.dto.pin.PinProcess
import com.procurement.access.model.dto.pin.PinTender
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.notice.exception.ErrorException
import com.procurement.notice.exception.ErrorType
import com.procurement.notice.model.bpe.ResponseDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface PinService {

    fun createPin(stage: String,
                  country: String,
                  owner: String,
                  dateTime: LocalDateTime,
                  pin: PinProcess): ResponseDto<*>
}

@Service
class PinServiceImpl(private val generationService: GenerationService,
                     private val tenderProcessDao: TenderProcessDao) : PinService {

    override fun createPin(stage: String,
                           country: String,
                           owner: String,
                           dateTime: LocalDateTime,
                           pin: PinProcess): ResponseDto<*> {
        validateFields(pin)
        val cpId = generationService.getCpId(country)
        pin.ocid = cpId
        pin.tender.apply {
            id = cpId
            procuringEntity.id = generationService.generateOrganizationId(procuringEntity)
            setStatuses(this)
            setItemsId(this)
            setLotsIdAndItemsAndDocumentsRelatedLots(this)
        }
        val entity = getEntity(pin, cpId, stage, dateTime, owner)
        tenderProcessDao.save(getEntity(pin, cpId, stage, dateTime, owner))
        pin.token = entity.token.toString()
        return ResponseDto(true, null, pin)
    }

    private fun validateFields(pin: PinProcess) {
        if (pin.tender.id != null) throw ErrorException(ErrorType.TENDER_ID_NOT_NULL)
        if (pin.tender.status != null) throw ErrorException(ErrorType.TENDER_STATUS_NOT_NULL)
        if (pin.tender.statusDetails != null) throw ErrorException(ErrorType.TENDER_STATUS_DETAILS_NOT_NULL)
        pin.tender.lots?.let { lots ->
            if (lots.asSequence().any({ lot -> lot.status != null })) throw ErrorException(ErrorType.LOT_STATUS_NOT_NULL)
            if (lots.asSequence().any({ lot -> lot.statusDetails != null })) throw ErrorException(ErrorType.LOT_STATUS_DETAILS_NOT_NULL)
        }
    }

    private fun setStatuses(tender: PinTender) {
        tender.status = PLANNED
        tender.statusDetails = EMPTY
        tender.lots?.forEach { lot ->
            lot.status = PLANNED
            lot.statusDetails = EMPTY
        }
    }

    private fun setItemsId(tender: PinTender) {
        tender.items?.forEach { it.id = generationService.generateTimeBasedUUID().toString() }
    }

    private fun setLotsIdAndItemsAndDocumentsRelatedLots(tender: PinTender) {
        tender.lots?.forEach { lot ->
            val id = generationService.generateTimeBasedUUID().toString()
            tender.items?.asSequence()
                    ?.filter { it.relatedLot == lot.id }
                    ?.forEach { it.relatedLot = id }
            tender.documents?.asSequence()
                    ?.filter { it.relatedLots != null }
                    ?.filter { it.relatedLots!!.contains(lot.id) }
                    ?.forEach { it.relatedLots!!.minus(lot.id).plus(id) }
            lot.id = id
        }
    }

    private fun getEntity(pin: PinProcess,
                          cpId: String,
                          stage: String,
                          dateTime: LocalDateTime,
                          owner: String): TenderProcessEntity {
        return TenderProcessEntity(
                cpId = cpId,
                token = generationService.generateRandomUUID(),
                stage = stage,
                owner = owner,
                createdDate = dateTime.toDate(),
                jsonData = toJson(pin)
        )
    }
}
