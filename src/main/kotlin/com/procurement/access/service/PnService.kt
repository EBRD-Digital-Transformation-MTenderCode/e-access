package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY
import com.procurement.access.model.dto.pn.Pn
import com.procurement.access.model.dto.pn.TenderPn
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface PnService {

    fun createPn(stage: String,
                 country: String,
                 owner: String,
                 dateTime: LocalDateTime,
                 pn: Pn): ResponseDto
}

@Service
class PnServiceImpl(private val generationService: GenerationService,
                    private val tenderProcessDao: TenderProcessDao) : PnService {

    override fun createPn(stage: String,
                          country: String,
                          owner: String,
                          dateTime: LocalDateTime,
                          pn: Pn): ResponseDto {
        validateFields(pn)
        val cpId = generationService.getCpId(country)
        pn.ocid = cpId
        pn.tender.apply {
            id = cpId
            procuringEntity.id = generationService.generateOrganizationId(procuringEntity)
            setStatuses(this)
            setItemsId(this)
            setLotsIdAndItemsAndDocumentsRelatedLots(this)
        }
        val entity = getEntity(pn, cpId, stage, dateTime, owner)
        tenderProcessDao.save(entity)
        pn.token = entity.token.toString()
        return ResponseDto(true, null, pn)
    }

    private fun validateFields(pn: Pn) {
        if (pn.tender.id != null) throw ErrorException(ErrorType.TENDER_ID_NOT_NULL)
        if (pn.tender.status != null) throw ErrorException(ErrorType.TENDER_STATUS_NOT_NULL)
        if (pn.tender.statusDetails != null) throw ErrorException(ErrorType.TENDER_STATUS_DETAILS_NOT_NULL)
        pn.tender.lots?.let { lots ->
            if (lots.asSequence().any({ lot -> lot.status != null })) throw ErrorException(ErrorType.LOT_STATUS_NOT_NULL)
            if (lots.asSequence().any({ lot -> lot.statusDetails != null })) throw ErrorException(ErrorType.LOT_STATUS_DETAILS_NOT_NULL)
        }
    }

    private fun setStatuses(tender: TenderPn) {
        tender.status = TenderStatus.PLANNING
        tender.statusDetails = EMPTY
        tender.lots?.forEach { lot ->
            lot.status = TenderStatus.PLANNING
            lot.statusDetails = EMPTY
        }
    }

    private fun setItemsId(tender: TenderPn) {
        tender.items?.forEach { it.id = generationService.generateTimeBasedUUID().toString() }
    }

    private fun setLotsIdAndItemsAndDocumentsRelatedLots(tender: TenderPn) {
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

    private fun getEntity(pn: Pn,
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
                jsonData = toJson(pn)
        )
    }
}
