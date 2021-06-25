package com.procurement.access.service

import com.procurement.access.application.model.MainMode
import com.procurement.access.application.model.Mode
import com.procurement.access.application.model.TestMode
import com.procurement.access.application.model.context.CheckExistanceItemsAndLotsContext
import com.procurement.access.application.model.context.CheckFEDataContext
import com.procurement.access.application.model.context.CheckNegotiationCnOnPnContext
import com.procurement.access.application.model.context.CheckOpenCnOnPnContext
import com.procurement.access.application.model.context.CheckSelectiveCnOnPnContext
import com.procurement.access.application.model.context.CreateSelectiveCnOnPnContext
import com.procurement.access.application.model.context.EvPanelsContext
import com.procurement.access.application.model.context.GetAwardCriteriaAndConversionsContext
import com.procurement.access.application.model.context.GetCriteriaForTendererContext
import com.procurement.access.application.model.context.GetItemsByLotsContext
import com.procurement.access.application.model.context.GetLotsAuctionContext
import com.procurement.access.application.model.params.GetMainProcurementCategoryParams
import com.procurement.access.application.service.CheckedNegotiationCnOnPn
import com.procurement.access.application.service.CheckedOpenCnOnPn
import com.procurement.access.application.service.CheckedSelectiveCnOnPn
import com.procurement.access.application.service.CreateNegotiationCnOnPnContext
import com.procurement.access.application.service.CreateOpenCnOnPnContext
import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.ap.create.ApCreateData
import com.procurement.access.application.service.ap.create.CreateApContext
import com.procurement.access.application.service.ap.get.GetAPTitleAndDescriptionContext
import com.procurement.access.application.service.ap.update.ApUpdateData
import com.procurement.access.application.service.ap.update.UpdateApContext
import com.procurement.access.application.service.cn.update.CnCreateContext
import com.procurement.access.application.service.cn.update.UpdateOpenCnContext
import com.procurement.access.application.service.cn.update.UpdateOpenCnData
import com.procurement.access.application.service.cn.update.UpdateSelectiveCnContext
import com.procurement.access.application.service.cn.update.UpdateSelectiveCnData
import com.procurement.access.application.service.cn.update.UpdatedOpenCn
import com.procurement.access.application.service.cn.update.UpdatedSelectiveCn
import com.procurement.access.application.service.fe.create.CreateFEContext
import com.procurement.access.application.service.fe.create.CreateFEData
import com.procurement.access.application.service.fe.update.AmendFEContext
import com.procurement.access.application.service.fe.update.AmendFEData
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
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId
import com.procurement.access.infrastructure.api.v1.ApiResponseV1
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.CommandTypeV1
import com.procurement.access.infrastructure.api.v1.businessError
import com.procurement.access.infrastructure.api.v1.commandId
import com.procurement.access.infrastructure.api.v1.country
import com.procurement.access.infrastructure.api.v1.cpid
import com.procurement.access.infrastructure.api.v1.internalServerError
import com.procurement.access.infrastructure.api.v1.isAuction
import com.procurement.access.infrastructure.api.v1.lotId
import com.procurement.access.infrastructure.api.v1.ocid
import com.procurement.access.infrastructure.api.v1.operationType
import com.procurement.access.infrastructure.api.v1.owner
import com.procurement.access.infrastructure.api.v1.phase
import com.procurement.access.infrastructure.api.v1.pmd
import com.procurement.access.infrastructure.api.v1.startDate
import com.procurement.access.infrastructure.api.v1.testMode
import com.procurement.access.infrastructure.api.v1.token
import com.procurement.access.infrastructure.configuration.properties.OCDSProperties
import com.procurement.access.infrastructure.handler.HistoryRepository
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v1.converter.toResponseDto
import com.procurement.access.infrastructure.handler.v1.model.request.AmendFERequest
import com.procurement.access.infrastructure.handler.v1.model.request.ApCreateRequest
import com.procurement.access.infrastructure.handler.v1.model.request.ApUpdateRequest
import com.procurement.access.infrastructure.handler.v1.model.request.CheckFEDataRequest
import com.procurement.access.infrastructure.handler.v1.model.request.CreateFERequest
import com.procurement.access.infrastructure.handler.v1.model.request.GetItemsByLotsRequest
import com.procurement.access.infrastructure.handler.v1.model.request.LotsForAuctionRequest
import com.procurement.access.infrastructure.handler.v1.model.request.NegotiationCnOnPnRequest
import com.procurement.access.infrastructure.handler.v1.model.request.OpenCnOnPnRequest
import com.procurement.access.infrastructure.handler.v1.model.request.PnCreateRequest
import com.procurement.access.infrastructure.handler.v1.model.request.PrepareCancellationRequest
import com.procurement.access.infrastructure.handler.v1.model.request.SelectiveCnOnPnRequest
import com.procurement.access.infrastructure.handler.v1.model.request.SetLotsStatusUnsuccessfulRequest
import com.procurement.access.infrastructure.handler.v1.model.request.UpdateOpenCnRequest
import com.procurement.access.infrastructure.handler.v1.model.request.UpdateSelectiveCnRequest
import com.procurement.access.infrastructure.handler.v1.model.response.AmendFEResponse
import com.procurement.access.infrastructure.handler.v1.model.response.ApCreateResponse
import com.procurement.access.infrastructure.handler.v1.model.response.CheckNegotiationCnOnPnResponse
import com.procurement.access.infrastructure.handler.v1.model.response.CheckOpenCnOnPnResponse
import com.procurement.access.infrastructure.handler.v1.model.response.CheckSelectiveCnOnPnResponse
import com.procurement.access.infrastructure.handler.v1.model.response.CreateFEResponse
import com.procurement.access.infrastructure.handler.v1.model.response.GetAwardCriteriaResponse
import com.procurement.access.infrastructure.handler.v1.model.response.GetLotResponse
import com.procurement.access.infrastructure.handler.v1.model.response.GetMainProcurementCategoryResponse
import com.procurement.access.infrastructure.handler.v1.model.response.LotsForAuctionResponse
import com.procurement.access.infrastructure.handler.v1.model.response.NegotiationCnOnPnResponse
import com.procurement.access.infrastructure.handler.v1.model.response.OpenCnOnPnResponse
import com.procurement.access.infrastructure.handler.v1.model.response.PnCreateResponse
import com.procurement.access.infrastructure.handler.v1.model.response.PrepareCancellationResponse
import com.procurement.access.infrastructure.handler.v1.model.response.SelectiveCnOnPnResponse
import com.procurement.access.infrastructure.handler.v1.model.response.SetLotsStatusUnsuccessfulResponse
import com.procurement.access.infrastructure.handler.v1.model.response.SetTenderUnsuccessfulResponse
import com.procurement.access.infrastructure.handler.v1.model.response.UpdateOpenCnResponse
import com.procurement.access.infrastructure.handler.v1.model.response.UpdateSelectiveCnResponse
import com.procurement.access.infrastructure.handler.v2.model.response.GetMainProcurementCategoryResult
import com.procurement.access.service.validation.JsonValidationService
import com.procurement.access.service.validation.ValidationService
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CommandServiceV1(
    private val historyRepository: HistoryRepository,
    private val pinService: PinService,
    private val pinOnPnService: PinOnPnService,
    private val pnService: PnService,
    private val apCreateService: ApCreateService,
    private val apService: APService,
    private val feCreateService: FeCreateService,
    private val feAmendService: FeAmendService,
    private val apUpdateService: ApUpdateService,
    private val apValidationService: ApValidationService,
    private val feValidationService: FeValidationService,
    private val pnUpdateService: PnUpdateService,
    private val cnCreateService: CnCreateService,
    private val cnService: CNService,
    private val selectiveCNService: SelectiveCNService,
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
    private val criteriaService: CriteriaService,
    private val logger: Logger
) {
    companion object {
        private val log = LoggerFactory.getLogger(CommandServiceV1::class.java)
    }

    private val testMode = ocdsProperties.prefixes!!.test!!
        .let { prefix ->
            TestMode(prefix = prefix, pattern = prefix.toRegex())
        }

    private val mainMode = ocdsProperties.prefixes!!.main!!
        .let { prefix ->
            MainMode(prefix = prefix, pattern = prefix.toRegex())
        }

    fun execute(cm: CommandMessage): ApiResponseV1 {
        val history = historyRepository.getHistory(cm.commandId, cm.command)
            .orThrow { it.exception }
        if (history != null) {
            return toObject(ApiResponseV1.Success::class.java, history)
        }
        val response: ApiResponseV1.Success = when (cm.command) {
            CommandTypeV1.CREATE_PIN -> pinService.createPin(cm)
            CommandTypeV1.CREATE_PN -> {
                val context = CreatePnContext(
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
                    log.debug("Create PN. Result: ${toJson(result)}")

                val response: PnCreateResponse = result.convert()
                if (log.isDebugEnabled)
                    log.debug("Create PN. Response: ${toJson(response)}")

                return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
            }
            CommandTypeV1.CREATE_AP -> {
                val context = CreateApContext(
                    owner = cm.owner,
                    pmd = cm.pmd,
                    country = cm.country,
                    startDate = cm.startDate,
                    mode = getMode(cm.testMode)
                )
                val request: ApCreateRequest = toObject(
                    ApCreateRequest::class.java, cm.data
                )
                val data: ApCreateData = request.convert()
                val result = apCreateService.createAp(context, data)
                if (log.isDebugEnabled)
                    log.debug("Create AP. Result: ${toJson(result)}")

                val response: ApCreateResponse = result.convert()
                if (log.isDebugEnabled)
                    log.debug("Create AP. Response: ${toJson(response)}")

                return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
            }
            CommandTypeV1.UPDATE_AP -> {
                val context = UpdateApContext(
                    ocid = cm.ocid,
                    owner = cm.owner,
                    cpid = cm.cpid,
                    token = cm.token,
                    startDate = cm.startDate
                )
                val request: ApUpdateRequest = toObject(ApUpdateRequest::class.java, cm.data)
                val data: ApUpdateData = request.convert()
                val response = apUpdateService.updateAp(context, data)

                if (log.isDebugEnabled)
                    log.debug("Update AP. Response: ${toJson(response)}")

                return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
            }
            CommandTypeV1.CREATE_FE -> {
                when (cm.pmd) {
                    ProcurementMethod.CF, ProcurementMethod.TEST_CF,
                    ProcurementMethod.OF, ProcurementMethod.TEST_OF -> {
                        val context = CreateFEContext(
                            cpid = cm.cpid,
                            ocid = cm.ocid,
                            startDate = cm.startDate,
                            owner = cm.owner
                        )
                        val request: CreateFERequest = toObject(CreateFERequest::class.java, cm.data)
                        val data: CreateFEData = request.convert()
                        val result = feCreateService.createFe(context, data)
                        if (log.isDebugEnabled)
                            log.debug("Create FE. Result: ${toJson(result)}")

                        val response: CreateFEResponse = result.convert()
                        if (log.isDebugEnabled)
                            log.debug("Create FE. Response: ${toJson(response)}")

                        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.MC, ProcurementMethod.TEST_MC,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandTypeV1.AMEND_FE -> {
                when (cm.pmd) {
                    ProcurementMethod.CF, ProcurementMethod.TEST_CF,
                    ProcurementMethod.OF, ProcurementMethod.TEST_OF -> {
                        val context = AmendFEContext(
                            cpid = cm.cpid,
                            startDate = cm.startDate,
                            ocid = cm.ocid,
                            owner = cm.owner
                        )
                        val request: AmendFERequest = toObject(AmendFERequest::class.java, cm.data)
                        val data: AmendFEData = request.convert()
                        val result = feAmendService.amendFe(context, data)
                        if (log.isDebugEnabled)
                            log.debug("Amend FE. Result: ${toJson(result)}")

                        val response: AmendFEResponse = result.convert()
                        if (log.isDebugEnabled)
                            log.debug("Amend FE. Response: ${toJson(response)}")

                        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.MC, ProcurementMethod.TEST_MC,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandTypeV1.UPDATE_PN -> pnUpdateService.updatePn(cm)
            CommandTypeV1.CREATE_CN -> {
                val context = CnCreateContext(
                    ocid = cm.ocid,
                    owner = cm.owner,
                    pmd = cm.pmd,
                    startDate = cm.startDate,
                    phase = cm.phase,
                    country = cm.country,
                    mode = getMode(cm.testMode)
                )

                cnCreateService.createCn(cm, context)
            }
            CommandTypeV1.UPDATE_CN -> {
                when (cm.pmd) {
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV -> {
                        val context = UpdateOpenCnContext(
                            cpid = cm.cpid,
                            token = cm.token,
                            ocid = cm.ocid,
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

                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT -> {
                        val context = UpdateSelectiveCnContext(
                            cpid = cm.cpid,
                            ocid = cm.ocid,
                            token = cm.token,
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

                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.MC, ProcurementMethod.TEST_MC,
                    ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
                    ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
                    ProcurementMethod.CF, ProcurementMethod.TEST_CF,
                    ProcurementMethod.OF, ProcurementMethod.TEST_OF,
                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandTypeV1.CREATE_PIN_ON_PN -> pinOnPnService.createPinOnPn(cm)
            CommandTypeV1.CREATE_CN_ON_PN -> {
                when (cm.pmd) {
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV -> {
                        val context = CreateOpenCnOnPnContext(
                            cpid = cm.cpid,
                            ocid = cm.ocid,
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
                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.MC, ProcurementMethod.TEST_MC,
                    ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
                    ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT -> {
                        val context = CreateSelectiveCnOnPnContext(
                            cpid = cm.cpid,
                            ocid = cm.ocid,
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
                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> {
                        val context = CreateNegotiationCnOnPnContext(
                            cpid = cm.cpid,
                            ocid = cm.ocid,
                            startDate = cm.startDate
                        )
                        val request: NegotiationCnOnPnRequest = toObject(NegotiationCnOnPnRequest::class.java, cm.data)
                        val response: NegotiationCnOnPnResponse =
                            negotiationCnOnPnService.create(context = context, data = request)
                                .also {
                                    if (log.isDebugEnabled)
                                        log.debug("Created CN on PN. Response: ${toJson(it)}")
                                }
                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.CF, ProcurementMethod.TEST_CF,
                    ProcurementMethod.OF, ProcurementMethod.TEST_OF,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandTypeV1.CREATE_REQUESTS_FOR_EV_PANELS -> {
                when (cm.pmd) {
                    ProcurementMethod.MC, ProcurementMethod.TEST_MC,
                    ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
                    ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> {
                        val context = EvPanelsContext(
                            cpid = cm.cpid,
                            ocid = cm.ocid,
                            owner = cm.owner,
                            startDate = cm.startDate
                        )
                        val response = criteriaService.createRequestsForEvPanels(context = context)
                            .also { result ->
                                if (log.isDebugEnabled)
                                    log.debug("Requests for EV panels was created. Result: ${toJson(result)}")
                            }
                            .convert()
                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.CF, ProcurementMethod.TEST_CF,
                    ProcurementMethod.OF, ProcurementMethod.TEST_OF,
                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandTypeV1.SET_TENDER_SUSPENDED -> tenderService.setSuspended(cm)
            CommandTypeV1.SET_TENDER_UNSUSPENDED -> tenderService.setUnsuspended(cm)
            CommandTypeV1.SET_TENDER_UNSUCCESSFUL -> {
                val context = SetTenderUnsuccessfulContext(
                    cpid = cm.cpid,
                    ocid = cm.ocid,
                    startDate = cm.startDate
                )

                val result = extendTenderService.setTenderUnsuccessful(context = context)
                if (log.isDebugEnabled)
                    log.debug("Tender status have been changed. Result: ${toJson(result)}")

                val dataResponse: SetTenderUnsuccessfulResponse = result.convert()
                if (log.isDebugEnabled)
                    log.debug("Tender status have been changed. Response: ${toJson(dataResponse)}")
                ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = dataResponse)
            }
            CommandTypeV1.SET_TENDER_PRECANCELLATION -> {
                val context = PrepareCancellationContext(
                    cpid = cm.cpid,
                    token = cm.token,
                    owner = cm.owner,
                    ocid = cm.ocid
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
                ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = dataResponse)
            }
            CommandTypeV1.SET_TENDER_CANCELLATION -> tenderService.setCancellation(cm)
            CommandTypeV1.SET_TENDER_STATUS_DETAILS -> tenderService.setStatusDetails(cm)
            CommandTypeV1.GET_TENDER_OWNER -> tenderService.getTenderOwner(cm)
            CommandTypeV1.GET_DATA_FOR_AC -> tenderService.getDataForAc(cm)
            CommandTypeV1.START_NEW_STAGE -> stageService.startNewStage(cm)
            CommandTypeV1.GET_ITEMS_BY_LOT -> lotsService.getItemsByLot(cm)
            CommandTypeV1.GET_ACTIVE_LOTS -> {
                when (cm.pmd) {
                    ProcurementMethod.MC, ProcurementMethod.TEST_MC,
                    ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
                    ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> {
                        val context = GetActiveLotsContext(
                            cpid = cm.cpid,
                            ocid = cm.ocid
                        )
                        val serviceResponse = lotsService.getActiveLots(context = context)
                        val response = serviceResponse.convert()
                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.CF, ProcurementMethod.TEST_CF,
                    ProcurementMethod.OF, ProcurementMethod.TEST_OF,
                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandTypeV1.GET_CRITERIA_FOR_TENDERER -> {
                val context = GetCriteriaForTendererContext(cpid = cm.cpid, ocid = cm.ocid)
                val result = criteriaService.getCriteriaForTenderer(context = context)

                if (log.isDebugEnabled)
                    log.debug("Criteria for tenderer. Result: ${toJson(result)}")

                val response = result.convert()
                if (log.isDebugEnabled)
                    log.debug("Criteria for tenderer. Response: ${toJson(response)}")

                ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
            }
            CommandTypeV1.GET_AP_TITLE_AND_DESCRIPTION -> {
                val context = GetAPTitleAndDescriptionContext(
                    cpid = cm.cpid,
                    ocid = cm.ocid
                )

                val result = apService.getAPTitleAndDescription(context = context)
                if (log.isDebugEnabled)
                    log.debug("AP title and description was found. Result: ${toJson(result)}")

                val response = result.convert()
                if (log.isDebugEnabled)
                    log.debug("AP title and description was found. Response: ${toJson(response)}")

                ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
            }
            CommandTypeV1.GET_LOT -> {
                val context = GetLotContext(
                    cpid = cm.cpid,
                    ocid = cm.ocid,
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
                ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = dataResponse)
            }
            CommandTypeV1.GET_LOTS_AUCTION -> {
                when (cm.pmd) {
                    ProcurementMethod.MC, ProcurementMethod.TEST_MC,
                    ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
                    ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> {
                        val context = GetLotsAuctionContext(cpid = cm.cpid, ocid = cm.ocid)
                        val serviceResponse = lotsService.getLotsAuction(context = context)
                        val response = serviceResponse.toResponseDto()
                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.CF, ProcurementMethod.TEST_CF,
                    ProcurementMethod.OF, ProcurementMethod.TEST_OF,
                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandTypeV1.GET_AWARD_CRITERIA -> {
                val context = GetAwardCriteriaContext(cpid = cm.cpid, ocid = cm.ocid)
                val result = extendTenderService.getAwardCriteria(context = context)
                if (log.isDebugEnabled)
                    log.debug("Tender award criteria. Result: ${toJson(result)}")

                val dataResponse: GetAwardCriteriaResponse = result.convert()
                if (log.isDebugEnabled)
                    log.debug("Tender award criteria. Response: ${toJson(dataResponse)}")
                ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = dataResponse)
            }
            CommandTypeV1.SET_LOTS_SD_UNSUCCESSFUL -> lotsService.setLotsStatusDetailsUnsuccessful(cm)
            CommandTypeV1.SET_LOTS_SD_AWARDED -> lotsService.setLotsStatusDetailsAwarded(cm)
            CommandTypeV1.SET_LOTS_UNSUCCESSFUL -> {
                val context = SetLotsStatusUnsuccessfulContext(
                    cpid = cm.cpid,
                    ocid = cm.ocid,
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
                ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = dataResponse)
            }
            CommandTypeV1.SET_FINAL_STATUSES -> lotsService.setFinalStatuses(cm)
            CommandTypeV1.SET_LOTS_INITIAL_STATUS -> lotsService.setLotInitialStatus(cm)
            CommandTypeV1.COMPLETE_LOTS -> lotsService.completeLots(cm)
            CommandTypeV1.CHECK_AWARD -> validationService.checkAward(cm)
            CommandTypeV1.CHECK_LOT_ACTIVE -> validationService.checkLotActive(cm)
            CommandTypeV1.CHECK_LOT_STATUS -> validationService.checkLotStatus(cm)
            CommandTypeV1.CHECK_LOTS_STATUS -> validationService.checkLotsStatus(cm)
            CommandTypeV1.CHECK_LOT_AWARDED -> validationService.checkLotAwarded(cm)
            CommandTypeV1.CHECK_BID -> validationService.checkBid(cm)
            CommandTypeV1.CHECK_ITEMS -> validationService.checkItems(cm)
            CommandTypeV1.CHECK_TOKEN -> validationService.checkToken(cm)
            CommandTypeV1.CHECK_CN_ON_PN -> {
                when (cm.pmd) {
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV -> {
                        val context = CheckOpenCnOnPnContext(
                            cpid = cm.cpid,
                            ocid = cm.ocid,
                            country = cm.country,
                            pmd = cm.pmd,
                            startDate = cm.startDate
                        )
                        val request: OpenCnOnPnRequest = toObject(OpenCnOnPnRequest::class.java, cm.data)
                        val result: CheckedOpenCnOnPn = cnOnPnService.check(context = context, data = request)
                        if (log.isDebugEnabled)
                            log.debug("Check CN on PN. Result: ${toJson(result)}")

                        val response = CheckOpenCnOnPnResponse(
                            requireAuction = result.requireAuction
                        )
                        if (log.isDebugEnabled)
                            log.debug("Check CN on PN. Response: ${toJson(response)}")

                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> {
                        val context = CheckNegotiationCnOnPnContext(
                            cpid = cm.cpid,
                            ocid = cm.ocid,
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

                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.MC, ProcurementMethod.TEST_MC,
                    ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
                    ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT -> {
                        val context = CheckSelectiveCnOnPnContext(
                            cpid = cm.cpid,
                            ocid = cm.ocid,
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

                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
                    }

                    ProcurementMethod.CF, ProcurementMethod.TEST_CF,
                    ProcurementMethod.OF, ProcurementMethod.TEST_OF,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandTypeV1.CHECK_EXISTANCE_ITEMS_AND_LOTS -> {
                when (cm.pmd) {
                    ProcurementMethod.CF, ProcurementMethod.TEST_CF,
                    ProcurementMethod.OF, ProcurementMethod.TEST_OF -> {
                        val context = CheckExistanceItemsAndLotsContext(cpid = cm.cpid, ocid = cm.ocid)
                        apValidationService.checkExistanceItemsAndLots(context = context)
                            .also { log.debug("Checking was a success.") }
                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = "ok")
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.MC, ProcurementMethod.TEST_MC,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandTypeV1.CHECK_FE_DATA -> {
                when (cm.pmd) {
                    ProcurementMethod.CF, ProcurementMethod.TEST_CF,
                    ProcurementMethod.OF, ProcurementMethod.TEST_OF -> {
                        val context = CheckFEDataContext(
                            cpid = cm.cpid,
                            ocid = cm.ocid,
                            operationType = cm.operationType,
                            startDate = cm.startDate
                        )
                        val request: CheckFEDataRequest = toObject(CheckFEDataRequest::class.java, cm.data)
                        feValidationService.checkFEData(context, request.convert())
                            .also { log.debug("Checking was a success.") }
                        ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = "ok")
                    }

                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.MC, ProcurementMethod.TEST_MC,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> throw ErrorException(ErrorType.INVALID_PMD)
                }
            }
            CommandTypeV1.VALIDATE_OWNER_AND_TOKEN -> validationService.checkAccessToTender(cm)
            CommandTypeV1.GET_LOTS_FOR_AUCTION -> {
                val context = LotsForAuctionContext(
                    cpid = cm.cpid,
                    ocid = cm.ocid,
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
                ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = dataResponse)
            }
            CommandTypeV1.GET_MAIN_PROCUREMENT_CATEGORY -> {
                val cpid = cm.cpid
                val ocid = cm.ocid
                val params = GetMainProcurementCategoryParams(cpid = cpid, ocid = ocid)
                val result: GetMainProcurementCategoryResult = tenderService.getMainProcurementCategory(params = params)
                    .onFailure { return responseError(version = cm.version, id = cm.commandId, fail = it.reason) }

                if (log.isDebugEnabled)
                    log.debug("Main procurement category. Result: ${toJson(result)}")

                val response = GetMainProcurementCategoryResponse(
                    mainProcurementCategory = result.tender.mainProcurementCategory.key
                )
                if (log.isDebugEnabled)
                    log.debug("Main procurement category. Response: ${toJson(response)}")

                ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
            }
            CommandTypeV1.GET_AWARD_CRITERIA_AND_CONVERSIONS -> {
                val response = when (cm.pmd) {
                    ProcurementMethod.MC, ProcurementMethod.TEST_MC,
                    ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
                    ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
                    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
                    ProcurementMethod.MV, ProcurementMethod.TEST_MV,
                    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                    ProcurementMethod.SV, ProcurementMethod.TEST_SV -> {
                        val context = GetAwardCriteriaAndConversionsContext(cpid = cm.cpid, ocid = cm.ocid)
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

                    ProcurementMethod.CF, ProcurementMethod.TEST_CF,
                    ProcurementMethod.OF, ProcurementMethod.TEST_OF,
                    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
                    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
                    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
                    ProcurementMethod.IP, ProcurementMethod.TEST_IP,
                    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                    ProcurementMethod.OP, ProcurementMethod.TEST_OP -> throw ErrorException(ErrorType.INVALID_PMD)
                }
                ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
            }
            CommandTypeV1.GET_ITEMS_BY_LOTS -> {
                val context = GetItemsByLotsContext(cpid = cm.cpid, ocid = cm.ocid)
                val request = toObject(GetItemsByLotsRequest::class.java, cm.data)
                val data = request.convert()

                val response = lotsService.getItemsByLots(context, data)
                    .also { result -> log.debug("Getting items by lots. Result: ${toJson(result)}") }
                    .convert()

                ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
            }
        }

        historyRepository.saveHistory(cm.commandId, cm.command, toJson(response))
        return response
    }

    fun getMode(isTestMode: Boolean): Mode = if (isTestMode) testMode else mainMode

    fun responseError(version: ApiVersion, id: CommandId, fail: Fail): ApiResponseV1.Failure {
        fail.logging(logger)
        return when (fail) {
            is Fail.Error -> ApiResponseV1.Failure.businessError(
                id = id,
                version = version,
                code = fail.code,
                description = fail.description
            )
            is Fail.Incident -> ApiResponseV1.Failure.internalServerError(
                id = id,
                version = version,
                description = fail.description
            )
        }
    }
}
