package com.procurement.access.service

import com.procurement.access.application.service.CheckCnOnPnContext
import com.procurement.access.application.service.CheckNegotiationCnOnPnContext
import com.procurement.access.application.service.CheckedCnOnPn
import com.procurement.access.application.service.CheckedNegotiationCnOnPn
import com.procurement.access.application.service.CreateCnOnPnContext
import com.procurement.access.application.service.CreateNegotiationCnOnPnContext
import com.procurement.access.application.service.cn.update.UpdateCnContext
import com.procurement.access.application.service.cn.update.UpdateCnData
import com.procurement.access.application.service.lot.LotService
import com.procurement.access.application.service.lot.LotsForAuctionContext
import com.procurement.access.application.service.lot.LotsForAuctionData
import com.procurement.access.application.service.tender.ExtendTenderService
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationContext
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationData
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.CheckCnOnPnResponse
import com.procurement.access.infrastructure.dto.cn.CnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.CnOnPnResponse
import com.procurement.access.infrastructure.dto.cn.NegotiationCnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.NegotiationCnOnPnResponse
import com.procurement.access.infrastructure.dto.cn.UpdateCnRequest
import com.procurement.access.infrastructure.dto.cn.update.UpdateCnResponse
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.dto.lot.LotsForAuctionRequest
import com.procurement.access.infrastructure.dto.lot.LotsForAuctionResponse
import com.procurement.access.infrastructure.dto.tender.prepare.cancellation.PrepareCancellationRequest
import com.procurement.access.infrastructure.dto.tender.prepare.cancellation.PrepareCancellationResponse
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.CommandType
import com.procurement.access.model.dto.bpe.ResponseDto
import com.procurement.access.model.dto.bpe.country
import com.procurement.access.model.dto.bpe.cpid
import com.procurement.access.model.dto.bpe.operationType
import com.procurement.access.model.dto.bpe.owner
import com.procurement.access.model.dto.bpe.pmd
import com.procurement.access.model.dto.bpe.prevStage
import com.procurement.access.model.dto.bpe.stage
import com.procurement.access.model.dto.bpe.startDate
import com.procurement.access.model.dto.bpe.token
import com.procurement.access.service.validation.ValidationService
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CommandService(
    private val historyDao: HistoryDao,
    private val pinService: PinService,
    private val pinOnPnService: PinOnPnService,
    private val pnService: PnService,
    private val pnUpdateService: PnUpdateService,
    private val cnCreateService: CnCreateService,
    private val cnService: CNService,
    private val cnOnPinService: CnOnPinService,
    private val cnOnPnService: CnOnPnService,
    private val negotiationCnOnPnService: NegotiationCnOnPnService,
    private val tenderService: TenderService,
    private val lotsService: LotsService,
    private val lotService: LotService,
    private val stageService: StageService,
    private val validationService: ValidationService,
    private val extendTenderService: ExtendTenderService
) {
    companion object {
        private val log = LoggerFactory.getLogger(CommandService::class.java)
    }

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
            CommandType.UPDATE_CN -> {
                when (cm.pmd) {
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV -> {
                        val context = UpdateCnContext(
                            cpid = cm.cpid,
                            token = cm.token,
                            stage = cm.stage,
                            owner = cm.owner,
                            pmd = cm.pmd,
                            startDate = cm.startDate
                        )
                        val request = toObject(UpdateCnRequest::class.java, cm.data)
                        val data: UpdateCnData = request.convert()
                        val result = cnService.update(context, data)
                        if (log.isDebugEnabled)
                            log.debug("Update CN. Result: ${toJson(result)}")

                        val response: UpdateCnResponse = result.convert()
                        if (log.isDebugEnabled)
                            log.debug("Update CN. Response: ${toJson(response)}")

                        ResponseDto(data = response)
                    }

                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandType.CREATE_PIN_ON_PN -> pinOnPnService.createPinOnPn(cm)
            CommandType.CREATE_CN_ON_PIN -> cnOnPinService.createCnOnPin(cm)
            CommandType.CREATE_CN_ON_PN -> {
                when (cm.pmd) {
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV -> {
                        val context = CreateCnOnPnContext(
                            cpid = cm.cpid,
                            previousStage = cm.prevStage,
                            stage = cm.stage,
                            country = cm.country,
                            pmd = cm.pmd,
                            startDate = cm.startDate
                        )
                        val request: CnOnPnRequest = toObject(CnOnPnRequest::class.java, cm.data)
                        val response: CnOnPnResponse = cnOnPnService.createCnOnPn(context = context, data = request)
                            .also {
                                if (log.isDebugEnabled)
                                    log.debug("Created CN on PN. Response: ${toJson(it)}")
                            }
                        ResponseDto(data = response)
                    }

                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> {
                        val context = CreateNegotiationCnOnPnContext(
                            cpid = cm.cpid,
                            previousStage = cm.prevStage,
                            stage = cm.stage,
                            startDate = cm.startDate
                        )
                        val request: NegotiationCnOnPnRequest = toObject(NegotiationCnOnPnRequest::class.java, cm.data)
                        val response: NegotiationCnOnPnResponse =
                            negotiationCnOnPnService.createNegotiationCnOnPn(context = context, data = request)
                                .also {
                                    if (log.isDebugEnabled)
                                        log.debug("Created CN on PN. Response: ${toJson(it)}")
                                }
                        ResponseDto(data = response)
                    }

                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA ->
                        throw ErrorException(ErrorType.INVALID_PMD)
                }
            }

            CommandType.SET_TENDER_SUSPENDED -> tenderService.setSuspended(cm)
            CommandType.SET_TENDER_UNSUSPENDED -> tenderService.setUnsuspended(cm)
            CommandType.SET_TENDER_UNSUCCESSFUL -> tenderService.setUnsuccessful(cm)
            CommandType.SET_TENDER_PRECANCELLATION -> {
                val context = PrepareCancellationContext(
                    cpid = cm.cpid,
                    token = cm.token,
                    owner = cm.owner,
                    stage = cm.stage,
                    operationType = cm.operationType
                )
                val request = toObject(PrepareCancellationRequest::class.java, cm.data)
                val data = PrepareCancellationData(
                    amendments = request.amendments.map { amendment ->
                        PrepareCancellationData.Amendment(
                            rationale = amendment.rationale,
                            description = amendment.description,
                            documents = amendment.documents?.map { document ->
                                PrepareCancellationData.Amendment.Document(
                                    documentType = document.documentType,
                                    id = document.id,
                                    title = document.title,
                                    description = document.description
                                )
                            }
                        )
                    }
                )
                val result = extendTenderService.prepareCancellation(context = context, data = data)
                if (log.isDebugEnabled)
                    log.debug("Award was evaluate. Result: ${toJson(result)}")

                val dataResponse =
                    PrepareCancellationResponse(
                        tender = PrepareCancellationResponse.Tender(
                            statusDetails = result.tender.statusDetails
                        )
                    )
                if (log.isDebugEnabled)
                    log.debug("Award was evaluate. Response: ${toJson(dataResponse)}")
                ResponseDto(data = dataResponse)
            }
            CommandType.SET_TENDER_CANCELLATION -> tenderService.setCancellation(cm)
            CommandType.SET_TENDER_STATUS_DETAILS -> tenderService.setStatusDetails(cm)
            CommandType.GET_TENDER_OWNER -> tenderService.getTenderOwner(cm)
            CommandType.GET_DATA_FOR_AC -> tenderService.getDataForAc(cm)
            CommandType.START_NEW_STAGE -> stageService.startNewStage(cm)

            CommandType.GET_ITEMS_BY_LOT -> lotsService.getItemsByLot(cm)
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
            CommandType.CHECK_LOT_ACTIVE -> validationService.checkLotActive(cm)
            CommandType.CHECK_LOT_STATUS -> validationService.checkLotStatus(cm)
            CommandType.CHECK_LOTS_STATUS -> validationService.checkLotsStatus(cm)
            CommandType.CHECK_LOT_AWARDED -> validationService.checkLotAwarded(cm)
            CommandType.CHECK_BID -> validationService.checkBid(cm)
            CommandType.CHECK_ITEMS -> validationService.checkItems(cm)
            CommandType.CHECK_TOKEN -> validationService.checkToken(cm)
            CommandType.CHECK_BUDGET_SOURCES -> validationService.checkBudgetSources(cm)
            CommandType.CHECK_CN_ON_PN -> {
                val response: CheckCnOnPnResponse = when (cm.pmd) {
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV -> {
                        val context = CheckCnOnPnContext(
                            cpid = cm.cpid,
                            previousStage = cm.prevStage,
                            country = cm.country,
                            pmd = cm.pmd,
                            startDate = cm.startDate
                        )
                        val request: CnOnPnRequest = toObject(CnOnPnRequest::class.java, cm.data)
                        val result: CheckedCnOnPn = cnOnPnService.checkCnOnPn(context = context, data = request)
                        if (log.isDebugEnabled)
                            log.debug("Check CN on PN. Result: ${toJson(result)}")

                        val response = CheckCnOnPnResponse(
                            requireAuction = result.requireAuction
                        )
                        if (log.isDebugEnabled)
                            log.debug("Check CN on PN. Response: ${toJson(response)}")

                        response
                    }
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> {
                        val context = CheckNegotiationCnOnPnContext(
                            cpid = cm.cpid,
                            previousStage = cm.prevStage,
                            startDate = cm.startDate
                        )
                        val request: NegotiationCnOnPnRequest = toObject(NegotiationCnOnPnRequest::class.java, cm.data)
                        val result: CheckedNegotiationCnOnPn =
                            negotiationCnOnPnService.checkNegotiationCnOnPn(context = context, data = request)
                        if (log.isDebugEnabled)
                            log.debug("Check negotiation CN on PN. Result: ${toJson(result)}")

                        val response = CheckCnOnPnResponse(
                            requireAuction = result.requireAuction
                        )
                        if (log.isDebugEnabled)
                            log.debug("Check negotiation CN on PN. Response: ${toJson(response)}")

                        response
                    }

                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA ->
                        throw ErrorException(ErrorType.INVALID_PMD)
                }

                ResponseDto(data = response)
            }

            CommandType.VALIDATE_OWNER_AND_TOKEN -> validationService.checkOwnerAndToken(cm)

            CommandType.GET_LOTS_FOR_AUCTION -> {
                val context = LotsForAuctionContext(
                    cpid = cm.cpid,
                    stage = cm.stage
                )
                val request = toObject(LotsForAuctionRequest::class.java, cm.data)
                val data = LotsForAuctionData(
                    lots = request.lots.map { lot ->
                        LotsForAuctionData.Lot(
                            id = lot.id,
                            value = lot.value.let { value ->
                                LotsForAuctionData.Lot.Value(
                                    amount = value.amount,
                                    currency = value.currency
                                )
                            }
                        )

                    }
                )
                val result = lotService.getLotsForAuction(context = context, data = data)
                if (log.isDebugEnabled)
                    log.debug("Lots for auction. Result: ${toJson(result)}")

                val dataResponse = LotsForAuctionResponse(
                    lots = result.lots.map { lot ->
                        LotsForAuctionResponse.Lot(
                            id = lot.id,
                            value = lot.value.let { value ->
                                LotsForAuctionResponse.Lot.Value(
                                    amount = value.amount,
                                    currency = value.currency
                                )
                            }
                        )

                    }
                )
                if (log.isDebugEnabled)
                    log.debug("Lots for auction. Response: ${toJson(dataResponse)}")
                ResponseDto(data = dataResponse)
            }
        }
        historyEntity = historyDao.saveHistory(cm.id, cm.command.value(), response)
        return toObject(ResponseDto::class.java, historyEntity.jsonData)
    }
}
