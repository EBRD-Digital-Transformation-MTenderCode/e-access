package com.procurement.access.service

import com.procurement.access.dao.HistoryDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.CommandType
import com.procurement.access.model.dto.bpe.ResponseDto
import com.procurement.access.model.dto.ocds.ProcurementMethod
import com.procurement.access.service.validation.ValidationService
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

@Service
class CommandService(
    private val historyDao: HistoryDao,
    private val pinService: PinService,
    private val pinOnPnService: PinOnPnService,
    private val pnService: PnService,
    private val pnUpdateService: PnUpdateService,
    private val cnCreateService: CnCreateService,
    private val cnUpdateService: CnUpdateService,
    private val cnOnPinService: CnOnPinService,
    private val cnOnPnService: CnOnPnService,
    private val negotiationCnOnPnService: NegotiationCnOnPnService,
    private val tenderService: TenderService,
    private val lotsService: LotsService,
    private val stageService: StageService,
    private val validationService: ValidationService
) {

    fun execute(cm: CommandMessage): ResponseDto {
        var historyEntity = historyDao.getHistory(cm.id, cm.command.value())
        if (historyEntity != null) {
            return toObject(ResponseDto::class.java, historyEntity.jsonData)
        }
        val response = when (cm.command) {
            CommandType.CREATE_PIN -> pinService.createPin(cm)
            CommandType.CREATE_PN -> pnService.createPn(cm)
            CommandType.UPDATE_PN -> pnUpdateService.updatePn(cm)
            CommandType.CREATE_CN -> cnCreateService.createCn(cm)
            CommandType.UPDATE_CN -> cnUpdateService.updateCn(cm)
            CommandType.CREATE_PIN_ON_PN -> pinOnPnService.createPinOnPn(cm)
            CommandType.CREATE_CN_ON_PIN -> cnOnPinService.createCnOnPin(cm)
            CommandType.CREATE_CN_ON_PN -> {
                val pmd = getPmd(cm)
                when (pmd) {
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV ->
                        cnOnPnService.createCnOnPn(cm)

                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP ->
                        negotiationCnOnPnService.createNegotiationCnOnPn(cm)

                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA ->
                        throw ErrorException(ErrorType.INVALID_PMD)
                }
            }

            CommandType.SET_TENDER_SUSPENDED -> tenderService.setSuspended(cm)
            CommandType.SET_TENDER_UNSUSPENDED -> tenderService.setUnsuspended(cm)
            CommandType.SET_TENDER_UNSUCCESSFUL -> tenderService.setUnsuccessful(cm)
            CommandType.SET_TENDER_PRECANCELLATION -> tenderService.setPreCancellation(cm)
            CommandType.SET_TENDER_CANCELLATION -> tenderService.setCancellation(cm)
            CommandType.SET_TENDER_STATUS_DETAILS -> tenderService.setStatusDetails(cm)
            CommandType.GET_TENDER_OWNER -> tenderService.getTenderOwner(cm)
            CommandType.GET_DATA_FOR_AC -> tenderService.getDataForAc(cm)
            CommandType.START_NEW_STAGE -> stageService.startNewStage(cm)

            CommandType.GET_LOTS -> lotsService.getLots(cm)
            CommandType.GET_LOTS_AUCTION -> lotsService.getLotsAuction(cm)
            CommandType.GET_AWARD_CRITERIA -> lotsService.getAwardCriteria(cm)
            CommandType.SET_LOTS_SD_UNSUCCESSFUL -> lotsService.setLotsStatusDetailsUnsuccessful(cm)
            CommandType.SET_LOTS_SD_AWARDED -> lotsService.setLotsStatusDetailsAwarded(cm)
            CommandType.SET_LOTS_UNSUCCESSFUL -> lotsService.setLotsStatusUnsuccessful(cm)
            CommandType.SET_FINAL_STATUSES -> lotsService.setFinalStatuses(cm)
            CommandType.SET_LOTS_INITIAL_STATUS -> lotsService.setLotInitialStatus(cm)
            CommandType.COMPLETE_LOTS -> lotsService.completeLots(cm)

            CommandType.CHECK_AWARD -> validationService.checkAward(cm)
            CommandType.CHECK_LOT_STATUS -> validationService.checkLotStatus(cm)
            CommandType.CHECK_LOTS_STATUS -> validationService.checkLotsStatus(cm)
            CommandType.CHECK_LOT_AWARDED -> validationService.checkLotAwarded(cm)
            CommandType.CHECK_BID -> validationService.checkBid(cm)
            CommandType.CHECK_ITEMS -> validationService.checkItems(cm)
            CommandType.CHECK_TOKEN -> validationService.checkToken(cm)
            CommandType.CHECK_BUDGET_SOURCES -> validationService.checkBudgetSources(cm)
            CommandType.CHECK_CN_ON_PN -> {
                val pmd = getPmd(cm)
                when (pmd) {
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV ->
                        cnOnPnService.checkCnOnPn(cm)

                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP ->
                        negotiationCnOnPnService.checkNegotiationCnOnPn(cm)

                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA ->
                        throw ErrorException(ErrorType.INVALID_PMD)
                }
            }

            CommandType.VALIDATE_OWNER_AND_TOKEN -> validationService.checkOwnerAndToken(cm)
        }
        historyEntity = historyDao.saveHistory(cm.id, cm.command.value(), response)
        return toObject(ResponseDto::class.java, historyEntity.jsonData)
    }

    private fun getPmd(cm: CommandMessage): ProcurementMethod {
        return cm.context.pmd
            ?.let {
                ProcurementMethod.valueOrException(it) {
                    ErrorException(ErrorType.INVALID_PMD)
                }
            }
            ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'pmd' attribute in context.")
    }
}