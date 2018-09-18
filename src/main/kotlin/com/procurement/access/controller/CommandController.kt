package com.procurement.access.controller

import com.procurement.access.exception.EnumException
import com.procurement.access.exception.ErrorException
import com.procurement.access.model.bpe.*
import com.procurement.access.model.bpe.CommandType.*
import com.procurement.access.service.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/command")
class CommandController(private val pinService: PinService,
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

    @PostMapping
    fun command(@RequestBody commandMessage: CommandMessage): ResponseEntity<ResponseDto> {
        return ResponseEntity(execute(commandMessage), HttpStatus.OK)
    }

    fun execute(cm: CommandMessage): ResponseDto {
        return when (cm.command) {
            CREATE_PIN -> pinService.createPin(cm)
            CREATE_PN -> pnService.createPn(cm)
            UPDATE_PN -> pnUpdateService.updatePn(cm)
            CREATE_CN -> cnCreateService.createCn(cm)
            UPDATE_CN -> cnUpdateService.updateCn(cm)
            CREATE_PIN_ON_PN -> pinOnPnService.createPinOnPn(cm)
            CREATE_CN_ON_PIN -> cnOnPinService.createCnOnPin(cm)
            CREATE_CN_ON_PN -> cnOnPnService.createCnOnPn(cm)

            SUSPEND_TENDER -> tenderService.suspendTender(cm)
            UNSUSPEND_TENDER -> tenderService.unsuspendTender(cm)
            UNSUCCESSFUL_TENDER -> tenderService.setUnsuccessful(cm)
            PREPARE_CANCELLATION -> tenderService.prepareCancellation(cm)
            TENDER_CANCELLATION -> tenderService.tenderCancellation(cm)
            START_NEW_STAGE -> stageService.startNewStage(cm)

            GET_LOTS -> lotsService.getLots(cm)
            UPDATE_LOT_STATUS_DETAILS -> lotsService.updateStatusDetails(cm)
            UPDATE_LOT_STATUS_DETAILS_BY_BID -> lotsService.updateStatusDetailsById(cm)
            UPDATE_LOTS -> lotsService.updateLots(cm)
            UPDATE_LOTS_EV -> lotsService.updateLotsEv(cm)
            CHECK_LOT_STATUS -> lotsService.checkStatus(cm)
            CHECK_LOT_GET_ITEMS -> lotsService.checkStatusDetailsGetItems(cm)
            CHECK_BID -> validationService.checkBid(cm)
            CHECK_ITEMS -> validationService.checkItems(cm)
            CHECK_TOKEN -> validationService.checkToken(cm)
        }
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception::class)
    fun exception(ex: Exception): ResponseDto {
        return when (ex) {
            is ErrorException -> getErrorExceptionResponseDto(ex)
            is EnumException -> getEnumExceptionResponseDto(ex)
            else -> getExceptionResponseDto(ex)
        }
    }
}



