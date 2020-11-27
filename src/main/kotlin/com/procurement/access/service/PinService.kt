package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.infrastructure.api.v1.ApiResponseV1
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.commandId
import com.procurement.access.model.dto.pin.PinProcess
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

@Service
class PinService(private val generationService: GenerationService,
                 private val tenderProcessDao: TenderProcessDao) {

    fun createPin(cm: CommandMessage): ApiResponseV1.Success {
//        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
//        val country = cm.context.country ?: throw ErrorException(CONTEXT)
//        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
//        val dateTime = cm.context.startDate?.toLocal() ?: throw ErrorException(CONTEXT)
        val pin = toObject(PinProcess::class.java, cm.data)

//        validateFields(pin)
//        val cpId = generationService.getCpId(country)
//        pin.ocid = cpId
//        pin.tender.apply {
//            id = cpId
//            procuringEntity.id = generationService.generateOrganizationId(procuringEntity)
//            setStatuses(this)
//            setItemsId(this)
//            setLotsIdAndItemsAndDocumentsRelatedLots(this)
//        }
//        val entity = getEntity(pin, cpId, stage, dateTime, owner)
//        tenderProcessDao.save(getEntity(pin, cpId, stage, dateTime, owner))
//        pin.token = entity.token.toString()
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = pin)
    }

//    private fun validateFields(pin: PinProcess) {
//        if (pin.tender.id != null) throw ErrorException(ErrorType.TENDER_ID_NOT_NULL)
//        if (pin.tender.status != null) throw ErrorException(ErrorType.TENDER_STATUS_NOT_NULL)
//        if (pin.tender.statusDetails != null) throw ErrorException(ErrorType.TENDER_STATUS_DETAILS_NOT_NULL)
//        pin.tender.lots?.let { lots ->
//            if (lots.asSequence().any({ lot -> lot.status != null })) throw ErrorException(ErrorType.LOT_STATUS_NOT_NULL)
//            if (lots.asSequence().any({ lot -> lot.statusDetails != null })) throw ErrorException(ErrorType.LOT_STATUS_DETAILS_NOT_NULL)
//        }
//    }
//
//    private fun setStatuses(tender: PinTender) {
//        tender.status = PLANNED
//        tender.statusDetails = EMPTY
//        tender.lots?.forEach { lot ->
//            lot.status = PLANNED
//            lot.statusDetails = EMPTY
//        }
//    }
//
//    private fun setItemsId(tender: PinTender) {
//        tender.items?.forEach { it.id = generationService.generateTimeBasedUUID().toString() }
//    }
//
//    private fun setLotsIdAndItemsAndDocumentsRelatedLots(tender: PinTender) {
//        tender.lots?.forEach { lot ->
//            val id = generationService.generateTimeBasedUUID().toString()
//            tender.items?.asSequence()
//                    ?.filter { it.relatedLot == lot.id }
//                    ?.forEach { it.relatedLot = id }
//            tender.documents?.asSequence()
//                    ?.filter { it.relatedLots != null }
//                    ?.filter { it.relatedLots!!.contains(lot.id) }
//                    ?.forEach { it.relatedLots!!.minus(lot.id).plus(id) }
//            lot.id = id
//        }
//    }
//
//    private fun getEntity(pin: PinProcess,
//                          cpId: String,
//                          stage: String,
//                          dateTime: LocalDateTime,
//                          owner: String): TenderProcessEntity {
//        return TenderProcessEntity(
//                cpId = cpId,
//                token = generationService.generateRandomUUID(),
//                stage = stage,
//                owner = owner,
//                createdDate = dateTime.toDate(),
//                jsonData = toJson(pin)
//        )
//    }
}
