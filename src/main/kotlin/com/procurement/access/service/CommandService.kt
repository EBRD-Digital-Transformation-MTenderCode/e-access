package com.procurement.access.service

import com.procurement.access.application.model.MainMode
import com.procurement.access.application.model.Mode
import com.procurement.access.application.model.TestMode
import com.procurement.access.application.model.context.CheckNegotiationCnOnPnContext
import com.procurement.access.application.model.context.CheckOpenCnOnPnContext
import com.procurement.access.application.model.context.CheckResponsesContext
import com.procurement.access.application.model.context.CheckSelectiveCnOnPnContext
import com.procurement.access.application.model.context.CreateSelectiveCnOnPnContext
import com.procurement.access.application.model.context.EvPanelsContext
import com.procurement.access.application.model.context.GetAwardCriteriaAndConversionsContext
import com.procurement.access.application.model.context.GetLotsAuctionContext
import com.procurement.access.application.service.CheckedNegotiationCnOnPn
import com.procurement.access.application.service.CheckedOpenCnOnPn
import com.procurement.access.application.service.CheckedSelectiveCnOnPn
import com.procurement.access.application.service.CreateNegotiationCnOnPnContext
import com.procurement.access.application.service.CreateOpenCnOnPnContext
import com.procurement.access.application.service.cn.update.CnCreateContext
import com.procurement.access.application.service.cn.update.UpdateOpenCnContext
import com.procurement.access.application.service.cn.update.UpdateOpenCnData
import com.procurement.access.application.service.cn.update.UpdateSelectiveCnContext
import com.procurement.access.application.service.cn.update.UpdateSelectiveCnData
import com.procurement.access.application.service.cn.update.UpdatedOpenCn
import com.procurement.access.application.service.cn.update.UpdatedSelectiveCn
import com.procurement.access.application.service.lot.GetActiveLotsContext
import com.procurement.access.application.service.lot.GetLotContext
import com.procurement.access.application.service.lot.LotService
import com.procurement.access.application.service.lot.LotsForAuctionContext
import com.procurement.access.application.service.lot.LotsForAuctionData
import com.procurement.access.application.service.lot.SetLotsStatusUnsuccessfulContext
import com.procurement.access.application.service.pn.create.CreatePnContext
import com.procurement.access.application.service.pn.create.PnCreateData
import com.procurement.access.application.service.tender.ExtendTenderService
import com.procurement.access.application.service.tender.strategy.get.awardCriteria.GetAwardCriteriaContext
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationContext
import com.procurement.access.application.service.tender.strategy.prepare.cancellation.PrepareCancellationData
import com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful.SetTenderUnsuccessfulContext
import com.procurement.access.config.properties.OCDSProperties
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.CheckResponsesRequest
import com.procurement.access.infrastructure.dto.cn.CheckNegotiationCnOnPnResponse
import com.procurement.access.infrastructure.dto.cn.CheckOpenCnOnPnResponse
import com.procurement.access.infrastructure.dto.cn.CheckSelectiveCnOnPnResponse
import com.procurement.access.infrastructure.dto.cn.NegotiationCnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.NegotiationCnOnPnResponse
import com.procurement.access.infrastructure.dto.cn.OpenCnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.OpenCnOnPnResponse
import com.procurement.access.infrastructure.dto.cn.SelectiveCnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.SelectiveCnOnPnResponse
import com.procurement.access.infrastructure.dto.cn.UpdateOpenCnRequest
import com.procurement.access.infrastructure.dto.cn.UpdateSelectiveCnRequest
import com.procurement.access.infrastructure.dto.cn.update.UpdateOpenCnResponse
import com.procurement.access.infrastructure.dto.cn.update.UpdateSelectiveCnResponse
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.dto.converter.toResponseDto
import com.procurement.access.infrastructure.dto.lot.GetLotResponse
import com.procurement.access.infrastructure.dto.lot.LotsForAuctionRequest
import com.procurement.access.infrastructure.dto.lot.LotsForAuctionResponse
import com.procurement.access.infrastructure.dto.lot.SetLotsStatusUnsuccessfulRequest
import com.procurement.access.infrastructure.dto.lot.SetLotsStatusUnsuccessfulResponse
import com.procurement.access.infrastructure.dto.pn.PnCreateRequest
import com.procurement.access.infrastructure.dto.pn.PnCreateResponse
import com.procurement.access.infrastructure.dto.pn.converter.convert
import com.procurement.access.infrastructure.dto.tender.get.awardCriteria.GetAwardCriteriaResponse
import com.procurement.access.infrastructure.dto.tender.prepare.cancellation.PrepareCancellationRequest
import com.procurement.access.infrastructure.dto.tender.prepare.cancellation.PrepareCancellationResponse
import com.procurement.access.infrastructure.dto.tender.set.tenderUnsuccessful.SetTenderUnsuccessfulResponse
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.CommandType
import com.procurement.access.model.dto.bpe.ResponseDto
import com.procurement.access.model.dto.bpe.country
import com.procurement.access.model.dto.bpe.cpid
import com.procurement.access.model.dto.bpe.isAuction
import com.procurement.access.model.dto.bpe.lotId
import com.procurement.access.model.dto.bpe.operationType
import com.procurement.access.model.dto.bpe.owner
import com.procurement.access.model.dto.bpe.phase
import com.procurement.access.model.dto.bpe.pmd
import com.procurement.access.model.dto.bpe.prevStage
import com.procurement.access.model.dto.bpe.stage
import com.procurement.access.model.dto.bpe.startDate
import com.procurement.access.model.dto.bpe.testMode
import com.procurement.access.model.dto.bpe.token
import com.procurement.access.service.validation.JsonValidationService
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
    private val selectiveCNService: SelectiveCNService,
    private val cnOnPinService: CnOnPinService,
    private val cnOnPnService: OpenCnOnPnService,
    private val selectiveCnOnPnService: SelectiveCnOnPnService,
    private val negotiationCnOnPnService: NegotiationCnOnPnService,
    private val tenderService: TenderService,
    private val lotsService: LotsService,
    private val lotService: LotService,
    private val stageService: StageService,
    private val validationService: ValidationService,
    private val extendTenderService: ExtendTenderService,
    private val ocdsProperties: OCDSProperties,
    private val medeiaValidationService: JsonValidationService,
    private val criteriaService: CriteriaService
) {
    companion object {
        private val log = LoggerFactory.getLogger(CommandService::class.java)
    }

    private val testMode = ocdsProperties.prefixes!!.test!!
        .let { prefix ->
            TestMode(prefix = prefix, pattern = prefix.toRegex())
        }

    private val mainMode = ocdsProperties.prefixes!!.main!!
        .let { prefix ->
            MainMode(prefix = prefix, pattern = prefix.toRegex())
        }

    fun execute(cm: CommandMessage): ResponseDto {
        var historyEntity = historyDao.getHistory(cm.id, cm.command.value())
        if (historyEntity != null) {
            return toObject(ResponseDto::class.java, historyEntity.jsonData)
        }
        val response = when (cm.command) {
            CommandType.CREATE_PIN -> pinService.createPin(cm)
            CommandType.CREATE_PN -> {
                val context = CreatePnContext(
                    stage = cm.stage,
                    owner = cm.owner,
                    pmd = cm.pmd,
                    country = cm.country,
                    startDate = cm.startDate,
                    mode = getMode(cm.testMode)
                )
                val request: PnCreateRequest = toObject(PnCreateRequest::class.java, cm.data)
                val data: PnCreateData = request.convert()
                val result = pnService.createPn(context, data)
                if (log.isDebugEnabled)
                    log.debug("Update CN. Result: ${toJson(result)}")

                val response: PnCreateResponse = result.convert()
                if (log.isDebugEnabled)
                    log.debug("Update CN. Response: ${toJson(response)}")

                return ResponseDto(data = response)
            }
            CommandType.UPDATE_PN -> pnUpdateService.updatePn(cm)
            CommandType.CREATE_CN -> {
                val context = CnCreateContext(
                    stage = cm.stage,
                    owner = cm.owner,
                    pmd = cm.pmd,
                    startDate = cm.startDate,
                    phase = cm.phase,
                    country = cm.country,
                    mode = getMode(cm.testMode)
                )

                cnCreateService.createCn(cm, context)
            }
            CommandType.UPDATE_CN -> {
                when (cm.pmd) {
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV -> {
                        val context = UpdateOpenCnContext(
                            cpid = cm.cpid,
                            token = cm.token,
                            stage = cm.stage,
                            owner = cm.owner,
                            pmd = cm.pmd,
                            startDate = cm.startDate,
                            isAuction = cm.isAuction
                        )
                        val request = toObject(UpdateOpenCnRequest::class.java, cm.data)
                        val data: UpdateOpenCnData = request.convert()
                        val result: UpdatedOpenCn = cnService.update(context, data)
                        if (log.isDebugEnabled)
                            log.debug("Update CN. Result: ${toJson(result)}")

                        val response: UpdateOpenCnResponse = result.convert()
                        if (log.isDebugEnabled)
                            log.debug("Update CN. Response: ${toJson(response)}")

                        ResponseDto(data = response)
                    }

                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT -> {
                        val context = UpdateSelectiveCnContext(
                            cpid = cm.cpid,
                            token = cm.token,
                            stage = cm.stage,
                            owner = cm.owner,
                            pmd = cm.pmd,
                            startDate = cm.startDate,
                            isAuction = cm.isAuction
                        )
                        val request: UpdateSelectiveCnRequest = toObject(UpdateSelectiveCnRequest::class.java, cm.data)
                        val data: UpdateSelectiveCnData = request.convert()
                        val result: UpdatedSelectiveCn = selectiveCNService.update(context, data)
                        if (log.isDebugEnabled)
                            log.debug("Update selective CN. Result: ${toJson(result)}")

                        val response: UpdateSelectiveCnResponse = result.convert()
                        if (log.isDebugEnabled)
                            log.debug("Update selective CN. Response: ${toJson(response)}")

                        ResponseDto(data = response)
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandType.CREATE_PIN_ON_PN -> pinOnPnService.createPinOnPn(cm)
            CommandType.CREATE_CN_ON_PIN -> cnOnPinService.createCnOnPin(cm)
            CommandType.CREATE_CN_ON_PN -> {
                when (cm.pmd) {
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV -> {
                        val context = CreateOpenCnOnPnContext(
                            cpid = cm.cpid,
                            previousStage = cm.prevStage,
                            stage = cm.stage,
                            country = cm.country,
                            pmd = cm.pmd,
                            startDate = cm.startDate
                        )
                        val request: OpenCnOnPnRequest = toObject(OpenCnOnPnRequest::class.java, cm.data)
                        val response: OpenCnOnPnResponse = cnOnPnService.create(context = context, data = request)
                            .also {
                                if (log.isDebugEnabled)
                                    log.debug("Created CN on PN. Response: ${toJson(it)}")
                            }
                        ResponseDto(data = response)
                    }

                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT -> {
                        val context = CreateSelectiveCnOnPnContext(
                            cpid = cm.cpid,
                            previousStage = cm.prevStage,
                            stage = cm.stage,
                            country = cm.country,
                            pmd = cm.pmd,
                            startDate = cm.startDate
                        )
                        val request: SelectiveCnOnPnRequest = toObject(SelectiveCnOnPnRequest::class.java, cm.data)
                        val response: SelectiveCnOnPnResponse =
                            selectiveCnOnPnService.create(context = context, data = request)
                                .also {
                                    if (log.isDebugEnabled)
                                        log.debug("Created CN on PN (GPA). Response: ${toJson(it)}")
                                }
                        ResponseDto(data = response)
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
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
                            negotiationCnOnPnService.create(context = context, data = request)
                                .also {
                                    if (log.isDebugEnabled)
                                        log.debug("Created CN on PN. Response: ${toJson(it)}")
                                }
                        ResponseDto(data = response)
                    }


                    ProcurementMethod.FA, ProcurementMethod.TEST_FA -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandType.CREATE_REQUESTS_FOR_EV_PANELS -> {
                when (cm.pmd) {
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> {
                        val context = EvPanelsContext(cpid = cm.cpid, stage = cm.stage, owner = cm.owner)
                        val response = criteriaService.createRequestsForEvPanels(context = context)
                            .also { result ->
                                if (log.isDebugEnabled)
                                    log.debug("Requests for EV panels was created. Result: ${toJson(result)}")
                            }
                            .convert()
                        ResponseDto(data = response)
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }

            CommandType.SET_TENDER_SUSPENDED -> tenderService.setSuspended(cm)
            CommandType.SET_TENDER_UNSUSPENDED -> tenderService.setUnsuspended(cm)
            CommandType.SET_TENDER_UNSUCCESSFUL -> {
                val context = SetTenderUnsuccessfulContext(
                    cpid = cm.cpid,
                    stage = cm.stage,
                    startDate = cm.startDate
                )

                val result = extendTenderService.setTenderUnsuccessful(context = context)
                if (log.isDebugEnabled)
                    log.debug("Tender status have been changed. Result: ${toJson(result)}")

                val dataResponse: SetTenderUnsuccessfulResponse = result.convert()
                if (log.isDebugEnabled)
                    log.debug("Tender status have been changed. Response: ${toJson(dataResponse)}")
                ResponseDto(data = dataResponse)
//                tenderService.setUnsuccessful(cm)
            }
            CommandType.SET_TENDER_PRECANCELLATION -> {
                val context = PrepareCancellationContext(
                    cpid = cm.cpid,
                    token = cm.token,
                    owner = cm.owner,
                    stage = cm.stage
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
            CommandType.GET_ACTIVE_LOTS -> {
                when (cm.pmd) {
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> {
                        val context = GetActiveLotsContext(
                            cpid = cm.cpid,
                            stage = cm.stage
                        )
                        val serviceResponse = lotsService.getActiveLots(context = context)
                        val response = serviceResponse.convert()
                        ResponseDto(data = response)
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }

            CommandType.GET_LOT -> {
                val context = GetLotContext(
                    cpid = cm.cpid,
                    stage = cm.stage,
                    lotId = cm.lotId
                )
                val result = lotService.getLot(context = context)
                if (log.isDebugEnabled)
                    log.debug("Lot was found. Result: ${toJson(result)}")

                val dataResponse = result.let { lot ->
                    GetLotResponse.Lot(
                        id = lot.id,
                        internalId = lot.internalId,
                        title = lot.title,
                        description = lot.description,
                        status = lot.status,
                        statusDetails = lot.statusDetails,
                        value = lot.value,
                        options = lot.options.map { option ->
                            GetLotResponse.Lot.Option(
                                hasOptions = option.hasOptions
                            )
                        },
                        variants = lot.variants.map { variant ->
                            GetLotResponse.Lot.Variant(
                                hasVariants = variant.hasVariants
                            )
                        },
                        renewals = lot.renewals.map { renewal ->
                            GetLotResponse.Lot.Renewal(
                                hasRenewals = renewal.hasRenewals
                            )
                        },
                        recurrentProcurement = lot.recurrentProcurement.map { recurrentProcurement ->
                            GetLotResponse.Lot.RecurrentProcurement(
                                isRecurrent = recurrentProcurement.isRecurrent
                            )
                        },
                        contractPeriod = lot.contractPeriod.let { contractPeriod ->
                            GetLotResponse.Lot.ContractPeriod(
                                startDate = contractPeriod.startDate,
                                endDate = contractPeriod.endDate
                            )
                        },
                        placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                            GetLotResponse.Lot.PlaceOfPerformance(
                                description = placeOfPerformance.description,
                                address = placeOfPerformance.address.let { address ->
                                    GetLotResponse.Lot.PlaceOfPerformance.Address(
                                        streetAddress = address.streetAddress,
                                        postalCode = address.postalCode,
                                        addressDetails = address.addressDetails.let { addressDetails ->
                                            GetLotResponse.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                country = addressDetails.country.let { country ->
                                                    GetLotResponse.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                        scheme = country.scheme,
                                                        id = country.id,
                                                        description = country.description,
                                                        uri = country.uri
                                                    )
                                                },
                                                region = addressDetails.region.let { region ->
                                                    GetLotResponse.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                        scheme = region.scheme,
                                                        id = region.id,
                                                        description = region.description,
                                                        uri = region.uri
                                                    )
                                                },
                                                locality = addressDetails.locality.let { locality ->
                                                    GetLotResponse.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                                        scheme = locality.scheme,
                                                        id = locality.id,
                                                        description = locality.description,
                                                        uri = locality.uri
                                                    )
                                                }

                                            )
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
                if (log.isDebugEnabled)
                    log.debug("Lot was found. Response: ${toJson(dataResponse)}")
                ResponseDto(data = dataResponse)
            }

            CommandType.GET_LOTS_AUCTION -> {
                when (cm.pmd) {
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> {
                        val context = GetLotsAuctionContext(
                            cpid = cm.cpid,
                            stage = cm.stage
                        )
                        val serviceResponse = lotsService.getLotsAuction(context = context)
                        val response = serviceResponse.toResponseDto()
                        ResponseDto(data = response)
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandType.GET_AWARD_CRITERIA -> {
                val context =
                    GetAwardCriteriaContext(
                        cpid = cm.cpid,
                        stage = cm.stage
                    )
                val result = extendTenderService.getAwardCriteria(context = context)
                if (log.isDebugEnabled)
                    log.debug("Tender award criteria. Result: ${toJson(result)}")

                val dataResponse: GetAwardCriteriaResponse = result.convert()
                if (log.isDebugEnabled)
                    log.debug("Tender award criteria. Response: ${toJson(dataResponse)}")
                ResponseDto(data = dataResponse)
//                lotsService.getAwardCriteria(cm)
            }
            CommandType.SET_LOTS_SD_UNSUCCESSFUL -> lotsService.setLotsStatusDetailsUnsuccessful(cm)
            CommandType.SET_LOTS_SD_AWARDED -> lotsService.setLotsStatusDetailsAwarded(cm)
            CommandType.SET_LOTS_UNSUCCESSFUL -> {
                val context = SetLotsStatusUnsuccessfulContext(
                    cpid = cm.cpid,
                    stage = cm.stage,
                    startDate = cm.startDate
                )
                val request: SetLotsStatusUnsuccessfulRequest =
                    toObject(SetLotsStatusUnsuccessfulRequest::class.java, cm.data)
                val result = lotService.setStatusUnsuccessful(
                    context = context,
                    data = request.convert()
                )
                if (log.isDebugEnabled)
                    log.debug("Lots statuses have been changed. Result: ${toJson(result)}")

                val dataResponse: SetLotsStatusUnsuccessfulResponse = result.convert()
                if (log.isDebugEnabled)
                    log.debug("Lots statuses have been changed. Response: ${toJson(dataResponse)}")
                ResponseDto(data = dataResponse)
            }
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
                when (cm.pmd) {
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV -> {
                        val context = CheckOpenCnOnPnContext(
                            cpid = cm.cpid,
                            previousStage = cm.prevStage,
                            country = cm.country,
                            pmd = cm.pmd,
                            startDate = cm.startDate
                        )
                        val request: OpenCnOnPnRequest = medeiaValidationService.validateCriteria(cm.data)
                        val result: CheckedOpenCnOnPn = cnOnPnService.check(context = context, data = request)
                        if (log.isDebugEnabled)
                            log.debug("Check CN on PN. Result: ${toJson(result)}")

                        val response = CheckOpenCnOnPnResponse(
                            requireAuction = result.requireAuction
                        )
                        if (log.isDebugEnabled)
                            log.debug("Check CN on PN. Response: ${toJson(response)}")

                        ResponseDto(data = response)
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> {
                        val context = CheckNegotiationCnOnPnContext(
                            cpid = cm.cpid,
                            previousStage = cm.prevStage,
                            startDate = cm.startDate
                        )
                        val request: NegotiationCnOnPnRequest = toObject(NegotiationCnOnPnRequest::class.java, cm.data)
                        val result: CheckedNegotiationCnOnPn =
                            negotiationCnOnPnService.check(context = context, data = request)
                        if (log.isDebugEnabled)
                            log.debug("Check negotiation CN on PN. Result: ${toJson(result)}")

                        val response =
                            CheckNegotiationCnOnPnResponse(
                                requireAuction = result.requireAuction
                            )
                        if (log.isDebugEnabled)
                            log.debug("Check negotiation CN on PN. Response: ${toJson(response)}")

                        ResponseDto(data = response)
                    }

                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT -> {
                        val context = CheckSelectiveCnOnPnContext(
                            cpid = cm.cpid,
                            previousStage = cm.prevStage,
                            country = cm.country,
                            pmd = cm.pmd,
                            startDate = cm.startDate
                        )
                        val request: SelectiveCnOnPnRequest = toObject(SelectiveCnOnPnRequest::class.java, cm.data)
                        val result: CheckedSelectiveCnOnPn =
                            selectiveCnOnPnService.check(context = context, data = request)
                        if (log.isDebugEnabled)
                            log.debug("Check CN on PN (GPA). Result: ${toJson(result)}")

                        val response = CheckSelectiveCnOnPnResponse(
                            requireAuction = result.requireAuction
                        )
                        if (log.isDebugEnabled)
                            log.debug("Check CN on PN (GPA). Response: ${toJson(response)}")

                        ResponseDto(data = response)
                    }

                    ProcurementMethod.FA, ProcurementMethod.TEST_FA -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandType.CHECK_RESPONSES -> {
                when (cm.pmd) {
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> {
                        val context = CheckResponsesContext(cpid = cm.cpid, stage = cm.stage, owner = cm.owner, pmd = cm.pmd)
                        val request: CheckResponsesRequest = toObject(CheckResponsesRequest::class.java, cm.data)

                        criteriaService.checkResponses(context = context, data = request.convert())
                            .also {
                                log.debug("Checking response was a success.")
                            }
                        ResponseDto(data = "ok")
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }

            CommandType.VALIDATE_OWNER_AND_TOKEN -> validationService.checkOwnerAndToken(cm)

            CommandType.GET_LOTS_FOR_AUCTION -> {
                val context = LotsForAuctionContext(
                    cpid = cm.cpid,
                    stage = cm.stage,
                    prevStage = cm.prevStage,
                    operationType = cm.operationType
                )
                val request = toObject(LotsForAuctionRequest::class.java, cm.data)
                val data = LotsForAuctionData(
                    lots = request.lots.map { lot ->
                        LotsForAuctionData.Lot(
                            id = lot.id,
                            value = lot.value
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
                            value = lot.value
                        )
                    }
                )
                if (log.isDebugEnabled)
                    log.debug("Lots for auction. Response: ${toJson(dataResponse)}")
                ResponseDto(data = dataResponse)
            }
            CommandType.GET_AWARD_CRITERIA_AND_CONVERSIONS -> {
                val response = when (cm.pmd) {
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> {
                        val context = GetAwardCriteriaAndConversionsContext(cpid = cm.cpid, stage = cm.stage)
                        criteriaService.getAwardCriteriaAndConversions(context = context)
                            .also { result ->
                                if (result != null)
                                    log.debug("Getting criteria. Result: ${toJson(result)}")
                                else
                                    log.debug("No criteria.")
                            }
                            ?.convert()
                            ?: Unit
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> throw ErrorException(ErrorType.INVALID_PMD)
                }
                ResponseDto(data = response)
            }
        }

        historyEntity = historyDao.saveHistory(cm.id, cm.command.value(), response)
        return toObject(ResponseDto::class.java, historyEntity.jsonData)
    }

    fun getMode(isTestMode: Boolean): Mode = if (isTestMode) testMode else mainMode
}
