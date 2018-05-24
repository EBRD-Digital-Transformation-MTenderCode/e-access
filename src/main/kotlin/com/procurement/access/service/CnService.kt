package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.model.dto.cn.CnProcess
import com.procurement.access.model.dto.cn.CnTender
import com.procurement.access.model.dto.ocds.TenderStatus.ACTIVE
import com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.notice.exception.ErrorException
import com.procurement.notice.exception.ErrorType
import com.procurement.notice.model.bpe.ResponseDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface CnService {

    fun createCn(stage: String,
                 country: String,
                 owner: String,
                 dateTime: LocalDateTime,
                 cn: CnProcess): ResponseDto<*>
}

@Service
class CnServiceImpl(private val generationService: GenerationService,
                    private val tenderProcessDao: TenderProcessDao) : CnService {

    override fun createCn(stage: String,
                          country: String,
                          owner: String,
                          dateTime: LocalDateTime,
                          cn: CnProcess): ResponseDto<*> {

        validateFields(cn)
        checkCurrency(cn)
        val cpId = generationService.getCpId(country)
        cn.ocid = cpId
        cn.tender.apply {
            id = cpId
            procuringEntity.id = generationService.generateOrganizationId(procuringEntity)
            setStatuses(this)
            setItemsId(this)
            setLotsIdAndItemsAndDocumentsRelatedLots(this)
        }
        val entity = getEntity(cn, cpId, stage, dateTime, owner)
        tenderProcessDao.save(entity)
        cn.token = entity.token.toString()
        return ResponseDto(true, null, cn)
    }

    private fun validateFields(cn: CnProcess) {
        if (cn.tender.id != null) throw ErrorException(ErrorType.TENDER_ID_NOT_NULL)
        if (cn.tender.status != null) throw ErrorException(ErrorType.TENDER_STATUS_NOT_NULL)
        if (cn.tender.statusDetails != null) throw ErrorException(ErrorType.TENDER_STATUS_DETAILS_NOT_NULL)
        cn.tender.lots?.let { lots ->
            if (lots.asSequence().any({ lot -> lot.status != null })) throw ErrorException(ErrorType.LOT_STATUS_NOT_NULL)
            if (lots.asSequence().any({ lot -> lot.statusDetails != null })) throw ErrorException(ErrorType.LOT_STATUS_DETAILS_NOT_NULL)
        }
    }

    private fun checkCurrency(cn: CnProcess) {
        val budget = cn.planning.budget
        if (budget.budgetBreakdown.asSequence().map { it.amount }.toSet().size > 1)
            throw ErrorException(ErrorType.INVALID_CURRENCY)
        if (budget.amount.currency != budget.budgetBreakdown[0].amount.currency)
            throw ErrorException(ErrorType.INVALID_CURRENCY)
        if (cn.tender.value.currency != budget.budgetBreakdown[0].amount.currency)
            throw ErrorException(ErrorType.INVALID_CURRENCY)
    }

    private fun setStatuses(tender: CnTender) {
        tender.status = ACTIVE
        tender.statusDetails = EMPTY
        tender.lots?.forEach { lot ->
            lot.status = ACTIVE
            lot.statusDetails = EMPTY
        }
    }

    private fun setItemsId(tender: CnTender) {
        tender.items?.forEach { it.id = generationService.generateTimeBasedUUID().toString() }
    }

    private fun setLotsIdAndItemsAndDocumentsRelatedLots(tender: CnTender) {
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

    private fun getEntity(dto: CnProcess,
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
                jsonData = toJson(dto)
        )
    }
}
