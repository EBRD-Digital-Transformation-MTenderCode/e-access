package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.model.dto.cn.CnLot
import com.procurement.access.model.dto.cn.CnProcess
import com.procurement.access.model.dto.cn.CnTender
import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import com.procurement.access.model.dto.pin.PinLot
import com.procurement.access.model.dto.pin.PinProcess
import com.procurement.access.model.dto.pin.PinTender
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.DateUtil
import com.procurement.access.utils.JsonUtil
import com.procurement.access.utils.toDate
import com.procurement.notice.exception.ErrorException
import com.procurement.notice.exception.ErrorType
import com.procurement.notice.model.bpe.ResponseDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

interface CnOnPinService {

    fun createCnOnPin(
            cpId: String,
            previousStage: String,
            stage: String,
            owner: String,
            token: String,
            dateTime: LocalDateTime,
            data: CnProcess): ResponseDto<*>

}

@Service
class CnOnPinServiceImpl(private val jsonUtil: JsonUtil,
                         private val dateUtil: DateUtil,
                         private val tenderProcessDao: TenderProcessDao) : CnOnPinService {

    override fun createCnOnPin(cpId: String,
                               previousStage: String,
                               stage: String,
                               owner: String,
                               token: String,
                               dateTime: LocalDateTime,
                               cn: CnProcess): ResponseDto<*> {

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, previousStage)
                ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        if (entity.cpId != cn.tender.id) throw ErrorException(ErrorType.INVALID_CPID_FROM_DTO)
        val pinProcess = jsonUtil.toObject(PinProcess::class.java, entity.jsonData)
        val pinTender = pinProcess.tender
        if (pinTender.tenderPeriod.startDate.toLocalDate() != dateTime.toLocalDate())
            throw ErrorException(ErrorType.INVALID_START_DATE)
        cn.planning = pinProcess.planning
        val cnTender = convertPinToCnTender(pinTender)
        setLotsToCnFromPin(pinTender, cnTender)
        setStatuses(cnTender)
        if (cn.tender.submissionLanguages != null) cnTender.submissionLanguages = cn.tender.submissionLanguages
        if (cn.tender.documents != null) cnTender.documents = cn.tender.documents
        cn.tender = cnTender
        validateLots(cnTender)
        tenderProcessDao.save(getEntity(cn, cpId, stage, entity.token, dateTime, owner))
        cn.ocId = cpId
        cn.token = entity.token.toString()
        return ResponseDto(true, null, cn)
    }

    private fun setLotsToCnFromPin(pinTender: PinTender, cnTender: CnTender) {
        if (pinTender.lots != null) {
            cnTender.lots = pinTender.lots.asSequence()
                    .map({ convertPinToCnLot(it) }).toList()
        }
    }

    private fun validateLots(cnTender: CnTender) {
        var lotsFromDocuments: HashSet<String>? = null
        if (cnTender.documents != null) {
            lotsFromDocuments = cnTender.documents!!.asSequence()
                    .filter({ it.relatedLots != null })
                    .flatMap({ it.relatedLots!!.asSequence() }).toHashSet()
        }
        if (cnTender.lots != null && lotsFromDocuments != null && !lotsFromDocuments.isEmpty()) {
            val lotsFromCn = cnTender.lots!!.asSequence()
                    .map({ it.id }).toHashSet()
            if (!lotsFromCn.containsAll(lotsFromDocuments)) throw ErrorException(ErrorType.INVALID_LOTS_RELATED_LOTS)
        }
    }

    private fun convertPinToCnLot(pinLot: PinLot): CnLot {
        return CnLot(
                id = pinLot.id,
                title = pinLot.title,
                description = pinLot.description,
                status = pinLot.status,
                statusDetails = pinLot.statusDetails,
                value = pinLot.value,
                options = pinLot.options,
                recurrentProcurement = pinLot.recurrentProcurement,
                renewals = pinLot.renewals,
                variants = pinLot.variants,
                contractPeriod = pinLot.contractPeriod,
                placeOfPerformance = pinLot.placeOfPerformance
        )
    }

    private fun convertPinToCnTender(pinTender: PinTender): CnTender {
        return CnTender(
                id = pinTender.id,
                title = pinTender.title,
                description = pinTender.description,
                status = pinTender.status,
                statusDetails = pinTender.statusDetails,
                classification = pinTender.classification,
                items = HashSet(pinTender.items),
                value = pinTender.value,
                procurementMethod = pinTender.procurementMethod,
                procurementMethodDetails = pinTender.procurementMethodDetails,
                procurementMethodRationale = pinTender.procurementMethodRationale,
                mainProcurementCategory = pinTender.mainProcurementCategory,
                additionalProcurementCategories = pinTender.additionalProcurementCategories,
                awardCriteria = pinTender.awardCriteria,
                submissionMethod = pinTender.submissionMethod,
                submissionMethodDetails = pinTender.submissionMethodDetails,
                eligibilityCriteria = pinTender.eligibilityCriteria,
                contractPeriod = pinTender.contractPeriod,
                procuringEntity = pinTender.procuringEntity,
                documents = pinTender.documents,
                lots = null,
                lotGroups = pinTender.lotGroups,
                acceleratedProcedure = pinTender.acceleratedProcedure,
                designContest = pinTender.designContest,
                electronicWorkflows = pinTender.electronicWorkflows,
                jointProcurement = pinTender.jointProcurement,
                legalBasis = pinTender.legalBasis,
                procedureOutsourcing = pinTender.procedureOutsourcing,
                procurementMethodAdditionalInfo = pinTender.procurementMethodAdditionalInfo,
                submissionLanguages = pinTender.submissionLanguages,
                submissionMethodRationale = pinTender.submissionMethodRationale,
                dynamicPurchasingSystem = pinTender.dynamicPurchasingSystem,
                framework = pinTender.framework,
                requiresElectronicCatalogue = pinTender.requiresElectronicCatalogue
        )
    }

    private fun setStatuses(cnTender: CnTender) {
        cnTender.status = TenderStatus.ACTIVE
        cnTender.statusDetails = TenderStatusDetails.EMPTY
        cnTender.lots?.forEach { lot ->
            lot.status = TenderStatus.ACTIVE
            lot.statusDetails = TenderStatusDetails.EMPTY
        }
    }

    private fun getEntity(cn: CnProcess,
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
                jsonData = jsonUtil.toJson(cn))
    }


}
