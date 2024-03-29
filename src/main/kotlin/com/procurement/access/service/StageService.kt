package com.procurement.access.service

import com.procurement.access.infrastructure.api.v1.ApiResponseV1
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.commandId
import com.procurement.access.infrastructure.repository.CassandraTenderProcessRepositoryV1
import org.springframework.stereotype.Service

@Service
class StageService(private val tenderRepository: CassandraTenderProcessRepositoryV1) {

    fun startNewStage(cm: CommandMessage): ApiResponseV1.Success {
//        val cpId = cm.context.country ?: throw ErrorException(CONTEXT)
//        val token = cm.context.pmd ?: throw ErrorException(CONTEXT)
//        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
//        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
//        val previousStage = cm.context.startDate?.toLocal() ?: throw ErrorException(CONTEXT)

//        val entity = tenderProcessDao.getByCpIdAndStage(cpId, previousStage)
//                ?: throw ErrorException(DATA_NOT_FOUND)
//        if (entity.owner != owner) throw ErrorException(INVALID_OWNER)
//        if (entity.token.toString() != token) throw ErrorException(INVALID_TOKEN)
//        val process = toObject(TenderProcess::class.java, entity.jsonData)
//        process.tender.apply {
//            if (status !== TenderStatus.ACTIVE) throw ErrorException(NOT_ACTIVE)
//            if (statusDetails !== TenderStatusDetails.EMPTY) throw ErrorException(NOT_INTERMEDIATE)
//            if (!isHaveActiveLots(lots)) throw ErrorException(NO_ACTIVE_LOTS)
//            filterLots(this)
//            filterItems(this)
//            filterDocuments(this)
//        }
//        val newEntity = getEntity(process, entity, newStage)
//        tenderProcessDao.save(newEntity)
//        process.token = newEntity.token.toString()
//        return ResponseDto(true, null, process)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = null)
    }
//
//    private fun isHaveActiveLots(lots: List<Lot>): Boolean {
//        return lots.asSequence()
//                .any { it.status == TenderStatus.ACTIVE && it.statusDetails == TenderStatusDetails.EMPTY }
//    }
//
//    private fun filterLots(tender: Tender) {
//        tender.lots = tender.lots.asSequence()
//                .filter { it.status == TenderStatus.ACTIVE && it.statusDetails == TenderStatusDetails.EMPTY }
//                .toList()
//    }
//
//
//    private fun filterItems(tender: Tender) {
//        if (tender.items != null) {
//            val lotsIds = tender.lots.asSequence().map { it.id }.toHashSet()
//            tender.items = tender.items!!.asSequence().filter { lotsIds.contains(it.relatedLot) }.toHashSet()
//        }
//    }
//
//    private fun filterDocuments(tender: Tender) {
//        if (tender.documents != null) {
//            val documentsAfterFilter = HashSet<Document>()
//            val lotsIds = tender.lots.asSequence().map { it.id }.toHashSet()
//            tender.documents!!.forEach { document ->
//                if (document.relatedLots == null) documentsAfterFilter.add(document)
//                else {
//                    if (document.relatedLots!!.isEmpty()) documentsAfterFilter.add(document)
//                    if (document.relatedLots!!.asSequence().any({ lotsIds.contains(it) })) documentsAfterFilter.add(document)
//                }
//            }
//            tender.documents = documentsAfterFilter.toList()
//        }
//    }
//
//    private fun getEntity(process: TenderProcess,
//                          entity: TenderProcessEntity,
//                          stage: String): TenderProcessEntity {
//
//        return TenderProcessEntity(
//                cpId = entity.cpId,
//                token = entity.token,
//                stage = stage,
//                owner = entity.owner,
//                createdDate = localNowUTC().toDate(),
//                jsonData = toJson(process)
//        )
//    }
}

