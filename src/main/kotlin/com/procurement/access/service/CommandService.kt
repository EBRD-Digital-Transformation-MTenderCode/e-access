package com.procurement.access.service

import com.procurement.access.dao.HistoryDao
import com.procurement.access.model.bpe.CommandMessage
import com.procurement.access.model.bpe.CommandType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

interface CommandService {

    fun execute(cm: CommandMessage): ResponseDto

}

@Service
class CommandServiceImpl(private val historyDao: HistoryDao,
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
                         private val validationService: ValidationService) : CommandService {


    override fun execute(cm: CommandMessage): ResponseDto {
        var historyEntity = historyDao.getHistory(cm.context.operationId, cm.command.value())
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

            CommandType.SET_SUSPENDED -> tenderService.setSuspended(cm)
            CommandType.UNSUSPEND_TENDER -> tenderService.setUnsuspended(cm)
            CommandType.UNSUCCESSFUL_TENDER -> tenderService.setUnsuccessful(cm)
            CommandType.PREPARE_CANCELLATION -> tenderService.setPreCancellation(cm)
            CommandType.TENDER_CANCELLATION -> tenderService.setCancellation(cm)
            CommandType.START_NEW_STAGE -> stageService.startNewStage(cm)

            CommandType.GET_LOTS -> lotsService.getLots(cm)
            CommandType.UPDATE_LOT_STATUS_DETAILS -> lotsService.updateStatusDetails(cm)
            CommandType.UPDATE_LOT_STATUS_DETAILS_BY_BID -> lotsService.updateStatusDetailsById(cm)
            CommandType.UPDATE_LOTS -> lotsService.updateLots(cm)
            CommandType.UPDATE_LOTS_EV -> lotsService.updateLotsEv(cm)
            CommandType.CHECK_LOT_STATUS -> lotsService.checkStatus(cm)
            CommandType.CHECK_LOT_GET_ITEMS -> lotsService.checkStatusDetailsGetItems(cm)
            CommandType.CHECK_BID -> validationService.checkBid(cm)
            CommandType.CHECK_ITEMS -> validationService.checkItems(cm)
            CommandType.CHECK_TOKEN -> validationService.checkToken(cm)
        }
        historyEntity = historyDao.saveHistory(cm.context.operationId, cm.command.value(), response)
        return toObject(ResponseDto::class.java, historyEntity.jsonData)
    }

}