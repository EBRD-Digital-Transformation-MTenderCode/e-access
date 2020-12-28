package com.procurement.access.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.procurement.access.application.model.context.GetLotsAuctionContext
import com.procurement.access.application.model.data.GetLotsAuctionResponseData
import com.procurement.access.application.model.params.CheckLotsStateParams
import com.procurement.access.application.model.params.DivideLotParams
import com.procurement.access.application.model.params.GetLotsValueParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.Transform
import com.procurement.access.application.service.lot.GetActiveLotsContext
import com.procurement.access.application.service.tender.strategy.get.lots.GetActiveLotsResult
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.rule.LotStatesRule
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.exception.ErrorType.CONTEXT
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.exception.ErrorType.NO_ACTIVE_LOTS
import com.procurement.access.infrastructure.api.v1.ApiResponseV1
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.commandId
import com.procurement.access.infrastructure.api.v1.pmd
import com.procurement.access.infrastructure.api.v1.stage
import com.procurement.access.infrastructure.entity.TenderLotValueInfo
import com.procurement.access.infrastructure.entity.TenderLotsFullInfo
import com.procurement.access.infrastructure.entity.TenderLotsInfo
import com.procurement.access.infrastructure.handler.v1.model.request.ActivationAcLot
import com.procurement.access.infrastructure.handler.v1.model.request.ActivationAcRq
import com.procurement.access.infrastructure.handler.v1.model.request.ActivationAcRs
import com.procurement.access.infrastructure.handler.v1.model.request.ActivationAcTender
import com.procurement.access.infrastructure.handler.v1.model.request.CanCancellationLot
import com.procurement.access.infrastructure.handler.v1.model.request.CanCancellationRq
import com.procurement.access.infrastructure.handler.v1.model.request.CanCancellationRs
import com.procurement.access.infrastructure.handler.v1.model.request.FinalLot
import com.procurement.access.infrastructure.handler.v1.model.request.FinalStatusesRq
import com.procurement.access.infrastructure.handler.v1.model.request.FinalStatusesRs
import com.procurement.access.infrastructure.handler.v1.model.request.FinalTender
import com.procurement.access.infrastructure.handler.v1.model.request.ItemDto
import com.procurement.access.infrastructure.handler.v1.model.request.UpdateLotByBidRq
import com.procurement.access.infrastructure.handler.v1.model.request.UpdateLotByBidRs
import com.procurement.access.infrastructure.handler.v1.model.request.UpdateLotsRq
import com.procurement.access.infrastructure.handler.v1.model.request.UpdateLotsRs
import com.procurement.access.infrastructure.handler.v1.model.response.GetItemsByLotRs
import com.procurement.access.infrastructure.handler.v1.model.response.GetLotsValueResult
import com.procurement.access.infrastructure.handler.v2.model.response.DivideLotResult
import com.procurement.access.lib.extension.getUnknownElements
import com.procurement.access.lib.extension.toSet
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.ocds.asMoney
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

@Service
class LotsService(
    private val tenderProcessDao: TenderProcessDao,
    private val tenderProcessRepository: TenderProcessRepository,
    private val generationService: GenerationService,
    private val rulesService: RulesService,
    private val transform: Transform
) {

    fun getActiveLots(context: GetActiveLotsContext): GetActiveLotsResult {
        val entity = tenderProcessDao.getByCpIdAndStage(context.cpid, context.stage)
            ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val activeLots = getLotsByStatus(process.tender.lots, LotStatus.ACTIVE)
            .map { activeLot ->
                GetActiveLotsResult.Lot(
                    id = LotId.fromString(activeLot.id)
                )
            }
            .toList()
        return GetActiveLotsResult(lots = activeLots)
    }

    fun getLotsAuction(context: GetLotsAuctionContext): GetLotsAuctionResponseData {
        val entity = tenderProcessDao.getByCpIdAndStage(context.cpid, context.stage)
            ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val activeLots = getLotsByStatus(process.tender.lots, LotStatus.ACTIVE)
            .toList()
            .takeIf { it.isNotEmpty() }
            ?.map {
                GetLotsAuctionResponseData.Tender.Lot(
                    id = LotId.fromString(it.id),
                    title = it.title!!,
                    description = it.description!!,
                    value = it.value.asMoney
                )
            } ?: throw ErrorException(NO_ACTIVE_LOTS)

        return GetLotsAuctionResponseData(
            tender = GetLotsAuctionResponseData.Tender(
                id = process.tender.id!!,
                title = process.tender.title,
                description = process.tender.description,
                lots = activeLots
            )
        )
    }

    fun setLotsStatusDetailsUnsuccessful(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val lotsDto = toObject(UpdateLotsRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.apply {
            setLotsStatusDetails(lots, lotsDto, LotStatusDetails.UNSUCCESSFUL)
        }
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ApiResponseV1.Success(
            version = cm.version,
            id = cm.commandId,
            data = UpdateLotsRs(
                tenderStatus = process.tender.status,
                tenderStatusDetails = process.tender.statusDetails,
                lots = process.tender.lots,
                items = null
            )
        )
    }

    fun setLotsStatusDetailsAwarded(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val dto = toObject(UpdateLotByBidRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        var statusDetails = if (dto.lotAwarded) {
            LotStatusDetails.AWARDED
        } else {
            LotStatusDetails.EMPTY
        }
        val updatedLot = setLotsStatusDetails(process.tender.lots, dto.lotId, statusDetails)
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = UpdateLotByBidRs(updatedLot))
    }

    fun setFinalStatuses(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val dto = toObject(FinalStatusesRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val lots = process.tender.lots

        var stageEnd = false
        var cpSuccess = false
        val lot = lots.asSequence()
            .firstOrNull { it.id == dto.lotId }
            ?: throw ErrorException(
                error = ErrorType.INVALID_LOT_ID,
                message = "Lot by id: '${dto.lotId}' is not found."
            )

        lot.status = LotStatus.UNSUCCESSFUL
        lot.statusDetails = LotStatusDetails.EMPTY

//      if all lots have lot.status == "unsuccessful" || "cancelled"
//      tender.status == "unsuccessful" && tender.statusDetails == "empty"
//      stageEnd == TRUE; cpSuccess == FALSE
        if (lots.all { it.status == LotStatus.UNSUCCESSFUL || it.status == LotStatus.CANCELLED }) {
            process.tender.apply {
                status = TenderStatus.UNSUCCESSFUL
                statusDetails = TenderStatusDetails.EMPTY
            }
            stageEnd = true
            cpSuccess = false
//      if at least one lot with lot.status == "active"
//      stageEnd ==  FALSE; cpSuccess == TRUE
        } else if (lots.asSequence().any { it.status == LotStatus.ACTIVE }) {
            stageEnd = false
            cpSuccess = true
//      if at least one lot with lot.status == "complete" && all other lots have lot.status == "unsuccessful" || "cancelled"
//      tender.status == "complete" && tender.statusDetails == "empty"
//      stageEnd == TRUE; cpSuccess == TRUE
        } else {
            val completeLot = lots.asSequence().firstOrNull { it.status == LotStatus.COMPLETE }
            if (completeLot != null) {
                if (lots.asSequence()
                        .filter { it.id != completeLot.id }
                        .all { it.status == LotStatus.UNSUCCESSFUL || it.status == LotStatus.CANCELLED || it.status == LotStatus.COMPLETE }) {
                    process.tender.apply {
                        status = TenderStatus.COMPLETE
                        statusDetails = TenderStatusDetails.EMPTY
                    }
                    stageEnd = true
                    cpSuccess = true
                }
            }
        }
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        val tenderRs =
            FinalTender(
                id = process.tender.id!!,
                status = process.tender.status,
                statusDetails = process.tender.statusDetails
            )
        return ApiResponseV1.Success(
            version = cm.version,
            id = cm.commandId,
            data = FinalStatusesRs(
                stageEnd = stageEnd,
                cpSuccess = cpSuccess,
                tender = tenderRs,
                lots = listOf(
                    FinalLot(id = lot.id, status = lot.status!!, statusDetails = lot.statusDetails!!)
                )
            )
        )
    }

    fun setLotInitialStatus(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.stage
        val dto = toObject(CanCancellationRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val lot = process.tender.lots.first { it.id == dto.lotId }
        lot.apply {
            status = LotStatus.ACTIVE
            statusDetails = LotStatusDetails.EMPTY
        }
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ApiResponseV1.Success(
            version = cm.version,
            id = cm.commandId,
            data = CanCancellationRs(
                lot = CanCancellationLot(
                    id = lot.id,
                    status = lot.status!!,
                    statusDetails = lot.statusDetails!!
                )
            )
        )
    }

    fun getItemsByLot(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val lotId = cm.context.id ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val items = process.tender.items.filter { it.relatedLot == lotId }
            .map { ItemDto(id = it.id) }
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = GetItemsByLotRs(items = items))
    }

    fun completeLots(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = when (cm.pmd) {
            ProcurementMethod.OT, ProcurementMethod.TEST_OT,
            ProcurementMethod.SV, ProcurementMethod.TEST_SV,
            ProcurementMethod.MV, ProcurementMethod.TEST_MV -> "EV"

            ProcurementMethod.CD, ProcurementMethod.TEST_CD,
            ProcurementMethod.DA, ProcurementMethod.TEST_DA,
            ProcurementMethod.DC, ProcurementMethod.TEST_DC,
            ProcurementMethod.IP, ProcurementMethod.TEST_IP,
            ProcurementMethod.NP, ProcurementMethod.TEST_NP,
            ProcurementMethod.OP, ProcurementMethod.TEST_OP -> "NP"

            ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
            ProcurementMethod.RT, ProcurementMethod.TEST_RT -> "TP"

            ProcurementMethod.MC, ProcurementMethod.TEST_MC,
            ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
            ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ -> "CO"

            ProcurementMethod.CF, ProcurementMethod.TEST_CF,
            ProcurementMethod.OF, ProcurementMethod.TEST_OF -> "FE"

            ProcurementMethod.FA, ProcurementMethod.TEST_FA -> throw ErrorException(ErrorType.INVALID_PMD)
        }
        val dto = toObject(ActivationAcRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)

        val updatedLots = process.tender.lots
            .asSequence()
            .filter { dto.relatedLots.contains(it.id) }
            .map { lot ->
                lot.status = LotStatus.COMPLETE
                lot.statusDetails = LotStatusDetails.EMPTY
                lot
            }
            .toList()

        val stageEnd = process.tender.lots.asSequence().none { it.status == LotStatus.ACTIVE }
        if (stageEnd) {
            process.tender.apply {
                status = TenderStatus.COMPLETE
                statusDetails = TenderStatusDetails.EMPTY
            }
        }
        entity.jsonData = toJson(process)
        tenderProcessDao.save(entity)
        return ApiResponseV1.Success(
            version = cm.version,
            id = cm.commandId,
            data = ActivationAcRs(
                tender = ActivationAcTender(
                    status = process.tender.status,
                    statusDetails = process.tender.statusDetails
                ),
                lots = updatedLots.map {
                    ActivationAcLot(id = it.id, status = it.status!!, statusDetails = it.statusDetails!!)
                },
                stageEnd = stageEnd
            )
        )
    }

    private fun getLotsByStatus(lots: List<Lot>, status: LotStatus): Sequence<Lot> {
        return lots.asSequence().filter { it.status == status }
    }

    private fun setLotsStatusDetails(lots: List<Lot>, updateLotsDto: UpdateLotsRq, statusDetails: LotStatusDetails) {
        if (lots.isEmpty()) throw ErrorException(NO_ACTIVE_LOTS)
        val lotsIds = updateLotsDto.unsuccessfulLots?.toSet { it.id } ?: emptySet()
        lots.forEach { lot ->
            if (lot.id in lotsIds) lot.statusDetails = statusDetails
        }
    }

    private fun setLotsStatusDetails(lots: List<Lot>, lotId: String, lotStatusDetails: LotStatusDetails): Lot {
        return lots.asSequence()
            .filter { it.id == lotId }
            .first()
            .apply { statusDetails = lotStatusDetails }
    }

    fun getLotsValue(params: GetLotsValueParams): Result<GetLotsValueResult, Fail> {
        val tenderProcessEntity = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
            .onFailure { return it }
            ?: return ValidationErrors.TenderNotFoundOnGetLotsValue(cpid = params.cpid, ocid = params.ocid).asFailure()

        val tenderProcess = tenderProcessEntity.jsonData
            .tryToObject(TenderLotValueInfo::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
            .onFailure { return it }

        val storedLotsById = tenderProcess.tender.lots.orEmpty().associateBy { it.id }
        val receivedLotsIds = params.tender.lots.toSet { it.id.toString() }

        val unknownLots = getUnknownElements(received = receivedLotsIds, known = storedLotsById.keys)
        if (unknownLots.isNotEmpty())
            return ValidationErrors.LotNotFoundOnGetLotsValue(unknownLots).asFailure()

        return receivedLotsIds
            .map { id -> storedLotsById.getValue(id).toResult() }
            .let { lots -> GetLotsValueResult(GetLotsValueResult.Tender(lots)) }
            .asSuccess()
    }

    fun checkLotsState(params: CheckLotsStateParams): ValidationResult<Fail> {
        val tenderProcessEntity = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return ValidationErrors.TenderNotFoundOnCheckLotsState(cpid = params.cpid, ocid = params.ocid)
                .asValidationFailure()

        val tenderProcess = tenderProcessEntity.jsonData
            .tryToObject(TenderLotsInfo::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
            .onFailure { return it.reason.asValidationFailure() }

        val storedLotsById = tenderProcess.tender.lots.orEmpty().associateBy { it.id }
        val receivedLotsIds = params.tender.lots.toSet { it.id }

        val validStates = rulesService.getValidLotStates(params.country, params.pmd, params.operationType)
            .onFailure { return it.reason.asValidationFailure() }

        receivedLotsIds.forEach { id ->
            val storedLot = storedLotsById[id.toString()]
                ?: return ValidationErrors.LotNotFoundOnCheckLotsState(id).asValidationFailure()
            checkLotState(storedLot, validStates)
                .doOnError { return it.asValidationFailure() }
        }
        return ValidationResult.ok()
    }

    private fun TenderLotValueInfo.Tender.Lot.toResult() =
        GetLotsValueResult.Tender.Lot(
            id = id,
            value = value.let { value ->
                GetLotsValueResult.Tender.Lot.Value(
                    amount = value.amount,
                    currency = value.currency
                )
            }
        )

    private fun checkLotState(
        lot: TenderLotsInfo.Tender.Lot,
        validStates: LotStatesRule
    ): ValidationResult<ValidationErrors> =
        if (lotStateIsValid(lot, validStates))
            ValidationResult.ok()
        else ValidationErrors.InvalidLotState(lot.id).asValidationFailure()

    private fun lotStateIsValid(storedLot: TenderLotsInfo.Tender.Lot, validStates: LotStatesRule): Boolean =
        validStates.any { validState ->
            storedLot.status == validState.status
                && validState.statusDetails?.equals(storedLot.statusDetails) ?: true
        }

    fun divideLot(params: DivideLotParams): Result<DivideLotResult, Fail> {
        val tenderProcessEntity = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
            .onFailure { return it }
            ?: return ValidationErrors.TenderNotFoundOnGetLotsValue(cpid = params.cpid, ocid = params.ocid).asFailure()

        val tenderProcess = tenderProcessEntity.jsonData
            .tryToObject(TenderLotsFullInfo::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
            .onFailure { return it }

        val receivedLotsIds = params.tender.lots.toSet { it.id }
        val dividedLotId = tenderProcess.tender.lots.first { it.id.toString() in receivedLotsIds }.id

        val generatedLotsByOldIds = getGeneratedLotsByOldIds(params, dividedLotId)
        val generatedLots = generatedLotsByOldIds.values
        val updatedLots = updateDividedLot(tenderProcess, dividedLotId) + generatedLots

        val newLotIdsByOldLotIds = generatedLotsByOldIds.mapValues { it.value.id }
        val updatedItems = getUpdatedItems(params, newLotIdsByOldLotIds, tenderProcess)

        saveLotsAndItems(updatedLots, updatedItems, tenderProcessEntity)
            .doOnError { return it.asFailure() }

        return generateResult(generatedLots, updatedLots, dividedLotId, params, updatedItems)
    }

    private fun generateResult(
        generatedLots: Collection<TenderLotsFullInfo.Tender.Lot>,
        updatedLots: List<TenderLotsFullInfo.Tender.Lot>,
        dividedLotId: LotId,
        params: DivideLotParams,
        updatedItems: List<TenderLotsFullInfo.Tender.Item>
    ): Result<DivideLotResult, Fail> {
        val resultingLots = generatedLots + updatedLots.find { it.id == dividedLotId }!!
        val itemsIds = params.tender.items.toSet { it.id }
        val resultingItems = updatedItems.filter { it.id in itemsIds }

        return DivideLotResult(
            tender = DivideLotResult.Tender(
                lots = resultingLots.map { lot ->
                    DivideLotResult.Tender.Lot(
                        id = lot.id,
                        status = lot.status,
                        statusDetails = lot.statusDetails,
                        internalId = lot.internalId,
                        title = lot.title,
                        description = lot.description,
                        value = lot.value?.let { value ->
                            DivideLotResult.Tender.Lot.Value(amount = value.amount, currency = value.currency)
                        },
                        contractPeriod = lot.contractPeriod?.let { contractPeriod ->
                            DivideLotResult.Tender.Lot.ContractPeriod(
                                startDate = contractPeriod.startDate,
                                endDate = contractPeriod.endDate
                            )
                        },
                        placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                            placeOfPerformance?.address?.let { address ->
                                DivideLotResult.Tender.Lot.PlaceOfPerformance(
                                    address = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address(
                                        streetAddress = address.streetAddress,
                                        postalCode = address.postalCode,
                                        addressDetails = address.addressDetails.let { addressDetails ->
                                            DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                country = addressDetails.country.let { country ->
                                                    DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                        id = country.id,
                                                        description = country.description,
                                                        scheme = country.scheme,
                                                        uri = country.uri
                                                    )
                                                },
                                                region = addressDetails.country.let { region ->
                                                    DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                        id = region.id,
                                                        description = region.description,
                                                        scheme = region.scheme,
                                                        uri = region.uri
                                                    )
                                                },
                                                locality = addressDetails.country.let { locality ->
                                                    DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                                        id = locality.id,
                                                        description = locality.description,
                                                        scheme = locality.scheme,
                                                        uri = locality.uri
                                                    )
                                                }
                                            )
                                        }
                                    )
                                )
                            }

                        }
                    )
                },
                items = resultingItems.map { item ->
                    DivideLotResult.Tender.Item(
                        id = item.id,
                        relatedLot = item.relatedLot,
                        internalId = item.internalId,
                        description = item.description,
                        quantity = item.quantity,
                        classification = item.classification
                            .let { classification ->
                                DivideLotResult.Tender.Item.Classification(
                                    id = classification.id,
                                    description = classification.description,
                                    scheme = classification.scheme
                                )
                            },
                        unit = item.unit
                            .let { unit ->
                                DivideLotResult.Tender.Item.Unit(
                                    id = unit.id,
                                    name = unit.name
                                )
                            },
                        additionalClassifications = item.additionalClassifications
                            ?.map { additionalClassification ->
                                DivideLotResult.Tender.Item.AdditionalClassification(
                                    id = additionalClassification.id,
                                    scheme = additionalClassification.scheme,
                                    description = additionalClassification.description
                                )
                            }
                    )

                }
            )
        ).asSuccess()
    }

    private fun saveLotsAndItems(
        updatedLots: List<TenderLotsFullInfo.Tender.Lot>,
        updatedItems: List<TenderLotsFullInfo.Tender.Item>,
        tenderProcessEntity: TenderProcessEntity
    ): ValidationResult<Fail> {
        val lotsTransformed = transform.tryToJsonNode(updatedLots).onFailure { return it.reason.asValidationFailure() }
        val itemsTransformed = transform.tryToJsonNode(updatedItems)
            .onFailure { return it.reason.asValidationFailure() }

        val jsonDataNode = transform
            .tryParse(tenderProcessEntity.jsonData)
            .onFailure { return it.reason.asValidationFailure() }

        val updatedJsonData = jsonDataNode.get("tender")
            .apply {
                this as ObjectNode
                replace("lots", lotsTransformed)
                replace("items", itemsTransformed)
            }
            .let { transform.tryToJson(it) }
            .onFailure { return it.reason.asValidationFailure() }

        val updatedEntity = tenderProcessEntity.copy(jsonData = updatedJsonData)

        tenderProcessRepository.save(updatedEntity)
            .onFailure { return it.reason.asValidationFailure() }

        return ValidationResult.ok()
    }

    private fun getUpdatedItems(
        params: DivideLotParams,
        newLotIdsByOldLotIds: Map<String, LotId>,
        tenderProcess: TenderLotsFullInfo
    ): List<TenderLotsFullInfo.Tender.Item> {
        val newLotIdsByItems = params.tender.items.associateBy(
            keySelector = { item -> item.id },
            valueTransform = { item -> newLotIdsByOldLotIds.getValue(item.relatedLot) }
        )
        val receivedItemsIds = newLotIdsByItems.keys

        return tenderProcess.tender.items.map { item ->
            if (item.id in receivedItemsIds) {
                item.copy(relatedLot = newLotIdsByItems.getValue(item.id))
            } else item
        }
    }

    private fun getGeneratedLotsByOldIds(
        params: DivideLotParams,
        dividedLotId: LotId
    ) = params.tender.lots
        .filter { lot -> lot.id != dividedLotId.toString() }
        .associateBy(
            keySelector = { lot -> lot.id },
            valueTransform = { lot -> generateLot(lot) }
        )

    private fun updateDividedLot(
        tenderProcess: TenderLotsFullInfo,
        dividedLotId: LotId
    ) = tenderProcess.tender.lots.map { lot ->
        if (lot.id == dividedLotId)
            lot.copy(status = LotStatus.CANCELLED, statusDetails = LotStatusDetails.EMPTY)
        else
            lot
    }

    private fun generateLot(lot: DivideLotParams.Tender.Lot) =
        TenderLotsFullInfo.Tender.Lot(
            id = generationService.lotId(),
            status = LotStatus.ACTIVE,
            statusDetails = LotStatusDetails.EMPTY,
            internalId = lot.internalId,
            title = lot.title,
            description = lot.description,
            value = lot.value?.let { value ->
                TenderLotsFullInfo.Tender.Lot.Value(amount = value.amount, currency = value.currency)
            },
            contractPeriod = lot.contractPeriod?.let { contractPeriod ->
                TenderLotsFullInfo.Tender.Lot.ContractPeriod(
                    startDate = contractPeriod.startDate,
                    endDate = contractPeriod.endDate
                )
            },
            placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                placeOfPerformance?.address?.let { address ->
                    TenderLotsFullInfo.Tender.Lot.PlaceOfPerformance(
                        address = TenderLotsFullInfo.Tender.Lot.PlaceOfPerformance.Address(
                            streetAddress = address.streetAddress,
                            postalCode = address.postalCode,
                            addressDetails = address.addressDetails.let { addressDetails ->
                                TenderLotsFullInfo.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                    country = addressDetails.country.let { country ->
                                        TenderLotsFullInfo.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                            id = country.id,
                                            description = country.description,
                                            scheme = country.scheme,
                                            uri = country.uri
                                        )
                                    },
                                    region = addressDetails.country.let { region ->
                                        TenderLotsFullInfo.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                            id = region.id,
                                            description = region.description,
                                            scheme = region.scheme,
                                            uri = region.uri
                                        )
                                    },
                                    locality = addressDetails.country.let { locality ->
                                        TenderLotsFullInfo.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                            id = locality.id,
                                            description = locality.description,
                                            scheme = locality.scheme,
                                            uri = locality.uri
                                        )
                                    }
                                )
                            }
                        )
                    )
                }

            }
        )
}