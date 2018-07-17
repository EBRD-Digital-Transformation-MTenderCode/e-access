package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.LotCn
import com.procurement.access.model.dto.cn.request.LotCnCreate
import com.procurement.access.model.dto.cn.request.TenderCnCreate
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY
import com.procurement.access.model.dto.pn.LotPn
import com.procurement.access.model.dto.pn.Pn
import com.procurement.access.model.dto.pn.TenderPn
import com.procurement.access.model.dto.pn.request.LotPnCreate
import com.procurement.access.model.dto.pn.request.PnCreate
import com.procurement.access.model.dto.pn.request.TenderPnCreate
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.HashSet

interface PnService {

    fun createPn(stage: String,
                 country: String,
                 owner: String,
                 dateTime: LocalDateTime,
                 pnDto: PnCreate): ResponseDto
}

@Service
class PnServiceImpl(private val generationService: GenerationService,
                    private val tenderProcessDao: TenderProcessDao) : PnService {

    override fun createPn(stage: String,
                          country: String,
                          owner: String,
                          dateTime: LocalDateTime,
                          pnDto: PnCreate): ResponseDto {

        val cpId = generationService.getCpId(country)
        val planningDto = pnDto.planning
        val tenderDto = pnDto.tender
        setItemsId(pnDto.tender)
        setLotsIdAndItemsAndDocumentsRelatedLots(pnDto.tender)
        pnDto.tender.procuringEntity.id = generationService.generateOrganizationId(pnDto.tender.procuringEntity)
        val pn = Pn(
                ocid = cpId,
                token = null,
                planning = Planning(
                        budget = Budget(
                                description = planningDto.budget.description,
                                amount = planningDto.budget.amount,
                                isEuropeanUnionFunded = planningDto.budget.isEuropeanUnionFunded,
                                budgetBreakdown = planningDto.budget.budgetBreakdown
                        ),
                        rationale = planningDto.rationale
                ),
                tender = TenderPn(
                        id = cpId,
                        status = TenderStatus.PLANNING,
                        statusDetails = EMPTY,
                        designContest = DesignContest(serviceContractAward = false),
                        electronicWorkflows = ElectronicWorkflows(useOrdering = false, acceptInvoicing = false, usePayment = false),
                        jointProcurement = JointProcurement(isJointProcurement = false),
                        procedureOutsourcing = ProcedureOutsourcing(procedureOutsourced = false),
                        framework = Framework(isAFramework = false),
                        dynamicPurchasingSystem = DynamicPurchasingSystem(hasDynamicPurchasingSystem = false),
                        lotGroups = listOf(LotGroup(optionToCombine = false)),
                        requiresElectronicCatalogue = false,
                        acceleratedProcedure = AcceleratedProcedure(isAcceleratedProcedure = false),
                        awardCriteria = AwardCriteria.PRICE_ONLY,
                        submissionMethod = listOf(SubmissionMethod.ELECTRONIC_SUBMISSION),
                        lots = setLots(tenderDto.lots),
                        procuringEntity = tenderDto.procuringEntity,
                        description = tenderDto.description,
                        procurementMethod = getPmd(pmd),
                        value = getValueFromLots(tenderDto.lots),
                        items = setItems(tenderDto.items),
                        title = tenderDto.title,
                        documents = tenderDto.documents,
                        classification = tenderDto.classification,
                        contractPeriod = setContractPeriod(tenderDto.lots),
                        tenderPeriod = Period(tenderDto.tenderPeriod.startDate, dateTime),
                        submissionMethodRationale = tenderDto.submissionMethodRationale,
                        submissionLanguages = null,
                        procurementMethodAdditionalInfo = null,
                        legalBasis = tenderDto.legalBasis,
                        eligibilityCriteria = tenderDto.eligibilityCriteria,
                        submissionMethodDetails = tenderDto.submissionMethodDetails,
                        additionalProcurementCategories = null,
                        mainProcurementCategory = tenderDto.mainProcurementCategory,
                        procurementMethodRationale = tenderDto.procurementMethodRationale,
                        procurementMethodDetails = tenderDto.procurementMethodDetails
                )
        )


//        cnDto.tender.procuringEntity.id = generationService.generateOrganizationId(cnDto.tender.procuringEntity)
//        pnDto.tender.apply {
//            id = cpId
//            procuringEntity.id = generationService.generateOrganizationId(procuringEntity)
//            setStatuses(this)
//            setItemsId(this)
//            setLotsIdAndItemsAndDocumentsRelatedLots(this)
//        }
//        val entity = getEntity(pnDto, cpId, stage, dateTime, owner)
//        tenderProcessDao.save(entity)
//        pnDto.token = entity.token.toString()
//        return ResponseDto(true, null, pnDto)
    }


//    private fun setStatuses(tender: TenderPn) {
//        tender.status = TenderStatus.PLANNING
//        tender.statusDetails = EMPTY
//        tender.lots?.forEach { lot ->
//            lot.status = TenderStatus.PLANNING
//            lot.statusDetails = EMPTY
//        }
//    }

    private fun setLots(lotsDto: HashSet<LotPnCreate>): HashSet<LotPn> {
        return lotsDto.asSequence().map { convertDtoLotToCnLot(it) }.toHashSet()
    }

    private fun setItemsId(tender: TenderPnCreate) {
        tender.items?.forEach { it.id = generationService.generateTimeBasedUUID().toString() }
    }

    private fun setLotsIdAndItemsAndDocumentsRelatedLots(tender: TenderPnCreate) {
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
