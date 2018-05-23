package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.localNowUTC
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import com.procurement.notice.exception.ErrorException
import com.procurement.notice.exception.ErrorType
import com.procurement.notice.model.bpe.ResponseDto
import org.springframework.stereotype.Service
import java.util.*

interface StageService {

    fun startNewStage(cpId: String,
                      token: String,
                      previousStage: String,
                      newStage: String,
                      owner: String): ResponseDto<*>
}

@Service
class StageServiceImpl(private val tenderProcessDao: TenderProcessDao) : StageService {

    override fun startNewStage(cpId: String,
                               token: String,
                               previousStage: String,
                               newStage: String,
                               owner: String): ResponseDto<*> {

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, previousStage)
                ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.apply {
            if (status !== TenderStatus.ACTIVE) throw ErrorException(ErrorType.NOT_ACTIVE)
            if (statusDetails !== TenderStatusDetails.EMPTY) throw ErrorException(ErrorType.NOT_INTERMEDIATE)
            if (!isHaveActiveLots(lots)) throw ErrorException(ErrorType.NO_ACTIVE_LOTS)
            filterLots(this)
            filterItems(this)
            filterDocuments(this)
        }
        val newEntity = getEntity(process, entity, newStage)
        tenderProcessDao.save(newEntity)
        process.token = newEntity.token.toString()
        return ResponseDto(true, null, process)
    }

    private fun isHaveActiveLots(lots: List<Lot>): Boolean {
        return lots.asSequence()
                .any { it.status == TenderStatus.ACTIVE && it.statusDetails == TenderStatusDetails.EMPTY }
    }

    private fun filterLots(tender: Tender) {
        tender.lots = tender.lots.asSequence()
                .filter { it.status == TenderStatus.ACTIVE && it.statusDetails == TenderStatusDetails.EMPTY }
                .toList()
    }


    private fun filterItems(tender: Tender) {
        if (tender.items != null) {
            val lotsIds = tender.lots.asSequence().map { it.id }.toHashSet()
            tender.items = tender.items!!.asSequence().filter { lotsIds.contains(it.relatedLot) }.toHashSet()
        }
    }

    private fun filterDocuments(tender: Tender) {
        if (tender.documents != null) {
            val documentsAfterFilter = HashSet<Document>()
            val lotsIds = tender.lots.asSequence().map { it.id }.toHashSet()
            tender.documents!!.forEach { document ->
                if (document.relatedLots == null) documentsAfterFilter.add(document)
                else {
                    if (document.relatedLots!!.isEmpty()) documentsAfterFilter.add(document)
                    if (document.relatedLots!!.asSequence().any({ lotsIds.contains(it) })) documentsAfterFilter.add(document)
                }
            }
            tender.documents = documentsAfterFilter.toList()
        }
    }

    private fun getEntity(process: TenderProcess,
                          entity: TenderProcessEntity,
                          stage: String): TenderProcessEntity {

        return TenderProcessEntity(
                cpId = entity.cpId,
                token = entity.token,
                stage = stage,
                owner = entity.owner,
                createdDate = localNowUTC().toDate(),
                jsonData = toJson(process)
        )
    }
}

