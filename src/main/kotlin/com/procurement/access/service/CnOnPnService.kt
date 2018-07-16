package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.LotCn
import com.procurement.access.model.dto.cn.Cn
import com.procurement.access.model.dto.cn.TenderCn
import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import com.procurement.access.model.dto.pn.PnLot
import com.procurement.access.model.dto.pn.PnProcess
import com.procurement.access.model.dto.pn.PnTender
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

interface CnOnPnService {

    fun createCnOnPn(
            cpId: String,
            previousStage: String,
            stage: String,
            owner: String,
            token: String,
            dateTime: LocalDateTime,
            cn: Cn): ResponseDto
}

@Service
class CnOnPnServiceImpl(private val tenderProcessDao: TenderProcessDao) : CnOnPnService {

    override fun createCnOnPn(cpId: String,
                              previousStage: String,
                              stage: String,
                              owner: String,
                              token: String,
                              dateTime: LocalDateTime,
                              cn: Cn): ResponseDto {

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, previousStage)
                ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        if (entity.cpId != cn.tender.id) throw ErrorException(ErrorType.INVALID_CPID_FROM_DTO)
        val pnProcess = toObject(PnProcess::class.java, entity.jsonData)
        val pnTender = pnProcess.tender
        val cnTender = cn.tender.copy(
                title = pnTender.title,
                description = pnTender.description,
                classification = pnTender.classification,
                legalBasis = pnTender.legalBasis,
                procurementMethod = pnTender.procurementMethod,
                procurementMethodDetails = pnTender.procurementMethodDetails,
                mainProcurementCategory = pnTender.mainProcurementCategory,
                procuringEntity = pnTender.procuringEntity
        )
        setLotsToCnFromPn(pnTender, cnTender)
        validateLots(cnTender)
        setStatuses(cnTender)
        cn.ocid = cpId
        cn.planning = pnProcess.planning
        cn.tender = cnTender
        tenderProcessDao.save(getEntity(cn, cpId, stage, entity.token, dateTime, owner))
        cn.token = entity.token.toString()
        return ResponseDto(true, null, cn)
    }

    private fun setLotsToCnFromPn(pnTender: PnTender, cnTender: TenderCn) {
        if (pnTender.lots != null && !pnTender.lots.isEmpty()) {
            cnTender.lots = pnTender.lots.asSequence().map({ convertPnToCnLot(it) }).toHashSet()
        }
    }

    private fun validateLots(cnTender: TenderCn) {
        if (cnTender.documents != null) {
            val lotsFromDocuments = cnTender.documents!!.asSequence()
                    .filter({ it.relatedLots != null })
                    .flatMap({ it.relatedLots!!.asSequence() }).toHashSet()
            if (cnTender.lots != null && !lotsFromDocuments.isEmpty()) {
                val lotsFromCn = cnTender.lots!!.asSequence().map({ it.id }).toHashSet()
                if (!lotsFromCn.containsAll(lotsFromDocuments)) throw ErrorException(ErrorType.INVALID_LOTS_RELATED_LOTS)
            }
        }
    }

    private fun setStatuses(cnTender: TenderCn) {
        cnTender.status = TenderStatus.ACTIVE
        cnTender.statusDetails = TenderStatusDetails.EMPTY
        cnTender.lots?.forEach { lot ->
            lot.status = TenderStatus.ACTIVE
            lot.statusDetails = TenderStatusDetails.EMPTY
        }
    }

    private fun convertPnToCnLot(pnLot: PnLot): LotCn {
        return LotCn(
                id = pnLot.id,
                title = pnLot.title,
                description = pnLot.description,
                status = pnLot.status,
                statusDetails = pnLot.statusDetails,
                value = pnLot.value,
                options = pnLot.options,
                recurrentProcurement = pnLot.recurrentProcurement,
                renewals = pnLot.renewals,
                variants = pnLot.variants,
                contractPeriod = pnLot.contractPeriod,
                placeOfPerformance = pnLot.placeOfPerformance
        )
    }

    private fun getEntity(cn: Cn,
                          cpId: String,
                          stage: String,
                          token: UUID,
                          dateTime: LocalDateTime,
                          owner: String): TenderProcessEntity {
        return TenderProcessEntity(
                cpId = cpId,
                token = token,
                stage = stage,
                owner = owner,
                createdDate = dateTime.toDate(),
                jsonData = toJson(cn)
        )
    }
}
