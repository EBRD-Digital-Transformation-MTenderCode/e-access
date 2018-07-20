package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.pin.PinProcess
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface PinOnPnService {

    fun createPinOnPn(
            cpId: String,
            token: String,
            owner: String,
            stage: String,
            previousStage: String,
            dateTime: LocalDateTime,
            pin: PinProcess): ResponseDto
}

@Service
class PinOnPnServiceImpl(private val tenderProcessDao: TenderProcessDao) : PinOnPnService {

    override fun createPinOnPn(cpId: String,
                               token: String,
                               owner: String,
                               stage: String,
                               previousStage: String,
                               dateTime: LocalDateTime,
                               pin: PinProcess): ResponseDto {

//        val entity = tenderProcessDao.getByCpIdAndStage(cpId, previousStage)
//                ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
//        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
//        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
//        if (entity.cpId != pin.tender.id) throw ErrorException(ErrorType.INVALID_CPID_FROM_DTO)
//        val pn = toObject(Pn::class.java, entity.jsonData)
//        addLotsToPinFromPn(pn.tender, pin.tender)
//        validateLots(pn.tender, pin.tender)
//        pin.planning = pn.planning
//        pin.tender.apply {
//            title = pn.tender.title
//            description = pn.tender.description
//            classification = pn.tender.classification
//            legalBasis = pn.tender.legalBasis
//            procurementMethod = pn.tender.procurementMethod
//            procurementMethodDetails = pn.tender.procurementMethodDetails
//            mainProcurementCategory = pn.tender.mainProcurementCategory
//            procuringEntity = pn.tender.procuringEntity
//            setStatuses(this)
//        }
//        tenderProcessDao.save(getEntity(pin, cpId, stage, entity.token, dateTime, owner))
//        pin.ocid = cpId
//        pin.token = entity.token.toString()
        return ResponseDto(true, null, pin)
    }

//    private fun validateLots(pnTender: TenderPn, pinTender: PinTender) {
//        if (pinTender.documents != null) {
//            val lotsFromDocuments = pinTender.documents.asSequence()
//                    .filter({ it.relatedLots != null })
//                    .flatMap({ it.relatedLots!!.asSequence() }).toHashSet()
//
//            if (pnTender.lots != null) {
//                val lotsFromPn = pnTender.lots.asSequence().map({ it.id }).toHashSet()
//                if (!lotsFromPn.containsAll(lotsFromDocuments)) throw ErrorException(ErrorType.INVALID_DOCS_RELATED_LOTS)
//            } else {
//                if (pinTender.lots != null) {
//                    val lotsFromPin = pinTender.lots!!.asSequence().map({ it.id }).toHashSet()
//                    if (!lotsFromPin.containsAll(lotsFromDocuments)) throw ErrorException(ErrorType.INVALID_DOCS_RELATED_LOTS)
//                }
//            }
//        }
//    }
//
//    private fun addLotsToPinFromPn(pnTender: TenderPn, pinTender: PinTender) {
//        if (pnTender.lots != null) {
//            pinTender.lots = pnTender.lots.asSequence().map { convertPnToPinLot(it) }.toList()
//        }
//    }
//
//    private fun convertPnToPinLot(pnLot: LotPn): PinLot {
//        return PinLot(
//                id = pnLot.id,
//                title = pnLot.title,
//                description = pnLot.description,
//                status = pnLot.status,
//                statusDetails = pnLot.statusDetails,
//                value = pnLot.value,
//                options = pnLot.options,
//                recurrentProcurement = pnLot.recurrentProcurement,
//                renewals = pnLot.renewals,
//                variants = pnLot.variants,
//                contractPeriod = pnLot.contractPeriod,
//                placeOfPerformance = pnLot.placeOfPerformance
//        )
//    }
//
//    private fun setStatuses(tender: PinTender) {
//        tender.status = TenderStatus.PLANNED
//        tender.statusDetails = TenderStatusDetails.EMPTY
//        tender.lots?.forEach { lot ->
//            lot.status = TenderStatus.PLANNED
//            lot.statusDetails = TenderStatusDetails.EMPTY
//        }
//    }
//
//    private fun getEntity(pin: PinProcess,
//                          cpId: String,
//                          stage: String,
//                          token: UUID,
//                          dateTime: LocalDateTime,
//                          owner: String): TenderProcessEntity {
//        return TenderProcessEntity(
//                cpId = cpId,
//                token = token,
//                stage = stage,
//                owner = owner,
//                createdDate = dateTime.toDate(),
//                jsonData = toJson(pin)
//        )
//    }
}
