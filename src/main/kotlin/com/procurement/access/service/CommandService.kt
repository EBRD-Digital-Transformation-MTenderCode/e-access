package com.procurement.access.service

import com.procurement.access.dao.HistoryDao
import com.procurement.access.model.bpe.CommandMessage
import com.procurement.access.model.bpe.CommandType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service


@Service
class CommandService(private val historyDao: HistoryDao,
                     private val pinService: PinService,
                     private val pinOnPnService: PinOnPnService,
                     private val pnService: PnService,
                     private val pnUpdateService: PnUpdateService,
                     private val cnCreateService: CnCreateService,
                     private val cnUpdateService: CnUpdateService,
                     private val cnOnPinService: CnOnPinService,
                     private val cnOnPnService: CnOnPnService,
                     private val tenderService: TenderService,
                     private val lotsService: LotsService,
                     private val stageService: StageService,
                     private val validationService: ValidationService) {


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
            CommandType.CREATE_CN_ON_PN -> cnOnPnService.createCnOnPn(cm)

            CommandType.SET_TENDER_SUSPENDED -> tenderService.setSuspended(cm)
            CommandType.SET_TENDER_UNSUSPENDED -> tenderService.setUnsuspended(cm)
            CommandType.SET_TENDER_UNSUCCESSFUL -> tenderService.setUnsuccessful(cm)
            CommandType.SET_TENDER_PRECANCELLATION -> tenderService.setPreCancellation(cm)
            CommandType.SET_TENDER_CANCELLATION -> tenderService.setCancellation(cm)
            CommandType.SET_TENDER_STATUS_DETAILS -> tenderService.setStatusDetails(cm)
            CommandType.START_NEW_STAGE -> stageService.startNewStage(cm)

            CommandType.GET_LOTS -> lotsService.getLots(cm)
            CommandType.GET_LOTS_AUCTION -> lotsService.getLotsAuction(cm)
            CommandType.SET_LOTS_SD_UNSUCCESSFUL -> lotsService.setLotsStatusDetailsUnsuccessful(cm)
            CommandType.SET_LOTS_SD_AWARDED -> lotsService.setLotsStatusDetailsAwarded(cm)
            CommandType.SET_LOTS_UNSUCCESSFUL -> lotsService.setLotsStatusUnsuccessful(cm)
            CommandType.CONTRACT_PREPARATION -> lotsService.awardedContractPreparation(cm)

            CommandType.CHECK_LOTS_STATUS_DETAILS -> validationService.checkLotsStatusDetails(cm)
            CommandType.CHECK_LOTS_STATUS -> validationService.checkLotsStatus(cm)
            CommandType.CHECK_BID -> validationService.checkBid(cm)
            CommandType.CHECK_ITEMS -> validationService.checkItems(cm)
            CommandType.CHECK_TOKEN -> validationService.checkToken(cm)
        }
        historyEntity = historyDao.saveHistory(cm.id, cm.command.value(), response)
        return toObject(ResponseDto::class.java, historyEntity.jsonData)
    }

}