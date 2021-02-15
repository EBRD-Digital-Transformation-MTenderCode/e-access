package com.procurement.access.service

import com.procurement.access.application.model.context.GetItemsByLotsContext
import com.procurement.access.application.model.context.GetLotsAuctionContext
import com.procurement.access.application.model.data.GetItemsByLotsData
import com.procurement.access.application.model.data.GetItemsByLotsResult
import com.procurement.access.application.model.data.GetLotsAuctionResponseData
import com.procurement.access.application.model.params.CheckLotsStateParams
import com.procurement.access.application.model.params.DivideLotParams
import com.procurement.access.application.model.params.GetLotsValueParams
import com.procurement.access.application.model.params.ValidateLotsDataForDivisionParams
import com.procurement.access.application.repository.TenderProcessRepository
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
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.TenderLotValueInfo
import com.procurement.access.infrastructure.entity.TenderLotsAndItemsInfo
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
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class LotsService(
    private val tenderProcessDao: TenderProcessDao,
    private val tenderProcessRepository: TenderProcessRepository,
    private val generationService: GenerationService,
    private val rulesService: RulesService
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

    fun validateLotsDataForDivision(params: ValidateLotsDataForDivisionParams): ValidationResult<Fail> {
        val entity = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return ValidationErrors.TenderNotFoundOnValidateLotsDataForDivision(params.cpid, params.ocid).asValidationFailure()

        val process = entity.jsonData.tryToObject(TenderLotsAndItemsInfo::class.java)
            .onFailure { return it.reason.asValidationFailure() }

        val receivedLotsByIds = params.tender.lots.associateBy { it.id }
        val storedLotsByIds = process.tender.lots.orEmpty().associateBy { it.id.toString() }

        val dividedLot = getDividedLot(receivedLotsByIds, storedLotsByIds)
            .onFailure { return it.reason.asValidationFailure() }

        val newLots = getNewLots(receivedLotsByIds, dividedLot)
            .onFailure { return it.reason.asValidationFailure() }

        checkForMissingParameters(newLots)
            .doOnError { return it.asValidationFailure() }

        checkLots(newLots, dividedLot, params.tender.items, process.tender.items.orEmpty())
            .doOnError { return it.asValidationFailure() }

        return ValidationResult.ok()
    }

    private fun getNewLots(
        receivedLotsByIds: Map<String, ValidateLotsDataForDivisionParams.Tender.Lot>,
        knownLot: TenderLotsAndItemsInfo.Tender.Lot
    ): Result<List<ValidateLotsDataForDivisionParams.Tender.Lot>, ValidationErrors.IncorrectNumberOfNewLots>  {
        val newLots = receivedLotsByIds.minus(knownLot.id.toString())
        val minimumNumberOfNewLots = 2
        return if (newLots.size < minimumNumberOfNewLots)
            ValidationErrors.IncorrectNumberOfNewLots().asFailure()
        else newLots.values.toList().asSuccess()
    }

    private fun getDividedLot(
        receivedLotsByIds: Map<String, ValidateLotsDataForDivisionParams.Tender.Lot>,
        storedLotsByIds: Map<String, TenderLotsAndItemsInfo.Tender.Lot>
    ): Result<TenderLotsAndItemsInfo.Tender.Lot, ValidationErrors.IncorrectNumberOfKnownLots> {
        val knownLotsIds = receivedLotsByIds.keys.intersect(storedLotsByIds.keys)
        val expectedNumberOfKnownLots = 1

        if (knownLotsIds.size != expectedNumberOfKnownLots)
            return ValidationErrors.IncorrectNumberOfKnownLots(knownLotsIds).asFailure()

        return storedLotsByIds.getValue(knownLotsIds.first()).asSuccess()
    }

    private fun checkForMissingParameters(newLots: List<ValidateLotsDataForDivisionParams.Tender.Lot>) : ValidationResult<Fail>{
        newLots.map { lot ->
            lot.title ?: return ValidationErrors.MissingTittleOnValidateLotsDataForDivision(lot.id).asValidationFailure()
            lot.description ?: return ValidationErrors.MissingDescriptionOnValidateLotsDataForDivision(lot.id).asValidationFailure()
            lot.value ?: return ValidationErrors.MissingValueOnValidateLotsDataForDivision(lot.id).asValidationFailure()
            lot.contractPeriod ?: return ValidationErrors.MissingContractPeriodOnValidateLotsDataForDivision(lot.id).asValidationFailure()
            lot.placeOfPerformance ?: return ValidationErrors.MissingPlaceOfPerformanceOnValidateLotsDataForDivision(lot.id).asValidationFailure()
        }
        return ValidationResult.ok()
    }

    private fun checkLots(
        newLots: List<ValidateLotsDataForDivisionParams.Tender.Lot>,
        dividedLot: TenderLotsAndItemsInfo.Tender.Lot,
        receivedItems: List<ValidateLotsDataForDivisionParams.Tender.Item>,
        storedItems: List<TenderLotsAndItemsInfo.Tender.Item>
    ) : ValidationResult<Fail>{
        checkLotsValue(newLots, dividedLot)
            .doOnError { return it.asValidationFailure() }

        checkLotsContractPeriod(newLots, dividedLot)
            .doOnError { return it.asValidationFailure() }

        //VR.COM-1.39.9, VR.COM-1.39.10
        checkThatAllItemsReceivedAndNoneMissing(dividedLot, receivedItems, storedItems)
            .doOnError { return it.asValidationFailure() }

        //VR.COM-1.39.8, VR.COM-1.39.11
        checkNewLotsToItemsRelations(newLots, receivedItems)
            .doOnError { return it.asValidationFailure() }

        return ValidationResult.ok()
    }

    private fun checkLotsValue(
        newLots: List<ValidateLotsDataForDivisionParams.Tender.Lot>,
        dividedLot: TenderLotsAndItemsInfo.Tender.Lot
    ): ValidationResult<Fail> {
        checkLotsCurrency(newLots, dividedLot)
            .doOnError { return it.asValidationFailure() }

        checkLotsAmount(newLots, dividedLot)
            .doOnError { return it.asValidationFailure() }

        return ValidationResult.ok()
    }

    private fun checkLotsCurrency(
        newLots: List<ValidateLotsDataForDivisionParams.Tender.Lot>,
        dividedLot: TenderLotsAndItemsInfo.Tender.Lot
    ): ValidationResult<Fail> {
        newLots.forEach { newLot ->
            if (newLot.value!!.currency != dividedLot.value.currency)
                return ValidationErrors.CurrencyDoesNotMatch(newLotId = newLot.id, dividedLotId = dividedLot.id)
                    .asValidationFailure()
        }
        return ValidationResult.ok()
    }

    private fun checkLotsAmount(
        newLots: List<ValidateLotsDataForDivisionParams.Tender.Lot>,
        dividedLot: TenderLotsAndItemsInfo.Tender.Lot
    ): ValidationResult<ValidationErrors> {
        val newAmount = calcAmount(newLots).onFailure { return ValidationResult.error(it.reason) }
        if (dividedLot.value.amount != newAmount)
            return ValidationErrors.InvalidAmount(dividedLotId = dividedLot.id)
                .asValidationFailure()
        return ValidationResult.ok()
    }

    private fun calcAmount(newLots: List<ValidateLotsDataForDivisionParams.Tender.Lot>): Result<BigDecimal, ValidationErrors.InvalidAmountOfLot> {
        var total = BigDecimal.ZERO
        newLots.forEach { lot ->
            val amount = lot.value!!.amount
            if (amount > BigDecimal.ZERO)
                total += amount
            else
                return ValidationErrors.InvalidAmountOfLot(lot.id).asFailure()
        }
        return total.setScale(2, RoundingMode.HALF_UP).asSuccess()
    }

    private fun checkLotsContractPeriod(
        newLots: List<ValidateLotsDataForDivisionParams.Tender.Lot>,
        dividedLot: TenderLotsAndItemsInfo.Tender.Lot
    ): ValidationResult<Fail> {
        newLots.forEach { newLot ->
            if (newLot.contractPeriod!!.startDate != dividedLot.contractPeriod.startDate)
                return ValidationErrors.InvalidContractPeriodStart(newLotId = newLot.id, dividedLotId = dividedLot.id)
                    .asValidationFailure()

            if (newLot.contractPeriod.endDate != dividedLot.contractPeriod.endDate)
                return ValidationErrors.InvalidContractPeriodEnd(newLotId = newLot.id, dividedLotId = dividedLot.id)
                    .asValidationFailure()
        }

        return ValidationResult.ok()
    }

    private fun checkNewLotsToItemsRelations(
        newLots: List<ValidateLotsDataForDivisionParams.Tender.Lot>,
        items: List<ValidateLotsDataForDivisionParams.Tender.Item>
    ): ValidationResult<Fail> {
        val itemsByRelatedLots = items.associateBy { it.relatedLot }
        val newLotIds = newLots.toSet { it.id }

        checkEachNewLotHasItem(newLotIds, itemsByRelatedLots)
            .doOnError { return it.asValidationFailure() }

        checkEachItemRelatesToNewLot(itemsByRelatedLots, newLotIds)
            .doOnError { return it.asValidationFailure() }

        return ValidationResult.ok()
    }

    private fun checkEachItemRelatesToNewLot(
        itemsByRelatedLots: Map<String, ValidateLotsDataForDivisionParams.Tender.Item>,
        newLotIds: Set<String>
    ): ValidationResult<Fail> {
        val unknownLots = itemsByRelatedLots.keys - newLotIds
        return if (unknownLots.isNotEmpty()) {
            val itemsWithUnknownLots = unknownLots.map { itemsByRelatedLots.getValue(it).id }
            ValidationErrors.ItemsNotLinkedToAnyNewLots(itemsWithUnknownLots)
                .asValidationFailure()
        } else ValidationResult.ok()
    }

    private fun checkEachNewLotHasItem(
        newLotIds: Set<String>,
        itemsByRelatedLots: Map<String, ValidateLotsDataForDivisionParams.Tender.Item>
    ): ValidationResult<Fail> {
        val lotsWithoutItems = newLotIds - itemsByRelatedLots.keys
        return if (lotsWithoutItems.isNotEmpty())
            ValidationErrors.LotDoesNotHaveRelatedItem(lotsWithoutItems.toList())
                .asValidationFailure()
        else ValidationResult.ok()
    }

    private fun checkThatAllItemsReceivedAndNoneMissing(
        dividedLot: TenderLotsAndItemsInfo.Tender.Lot,
        receivedItems: List<ValidateLotsDataForDivisionParams.Tender.Item>,
        storedItems: List<TenderLotsAndItemsInfo.Tender.Item>
    ): ValidationResult<Fail> {
        val receivedItems = receivedItems.toSet { it.id }

        val storedItemsOfDividedLot = storedItems.asSequence()
            .filter { it.relatedLot == dividedLot.id }
            .map { it.id }
            .toSet()

        val missingItems = storedItemsOfDividedLot - receivedItems
        if (missingItems.isNotEmpty())
            return ValidationErrors.MissingItemsOfDividedLot(dividedLot.id, missingItems.toList())
                .asValidationFailure()

        val unknownItems = receivedItems - storedItemsOfDividedLot
        if (unknownItems.isNotEmpty())
            return ValidationErrors.UnknownItemsOfDividedLot(dividedLot.id, unknownItems.toList())
                .asValidationFailure()

        return ValidationResult.ok()
    }

    fun divideLot(params: DivideLotParams): Result<DivideLotResult, Fail> {
        val tenderProcessEntity = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
            .onFailure { return it }
            ?: return ValidationErrors.TenderNotFoundOnGetLotsValue(cpid = params.cpid, ocid = params.ocid).asFailure()

        val tenderProcess = tenderProcessEntity.jsonData
            .tryToObject(CNEntity::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
            .onFailure { return it }

        val receivedLotsIds = params.tender.lots.toSet { it.id }
        val dividedLotId = tenderProcess.tender.lots.first { it.id in receivedLotsIds }.id

        val generatedLotsByOldIds = getGeneratedLotsByOldIds(params, dividedLotId)
        val generatedLots = generatedLotsByOldIds.values
        val updatedLots = updateDividedLot(tenderProcess, dividedLotId) + generatedLots

        val newLotIdsByOldLotIds = generatedLotsByOldIds.mapValues { it.value.id }
        val updatedItems = getUpdatedItems(params, newLotIdsByOldLotIds, tenderProcess)

        val updatedTenderProcess = tenderProcess.copy(tender = tenderProcess.tender.copy(lots = updatedLots, items = updatedItems))
        val updatedTenderProcessEntity = tenderProcessEntity.copy(jsonData = toJson(updatedTenderProcess))
        tenderProcessRepository.save(updatedTenderProcessEntity)
            .onFailure { return it }

        return generateResult(generatedLots, updatedLots, dividedLotId, params, updatedItems)
    }

    private fun generateResult(
        generatedLots: Collection<CNEntity.Tender.Lot>,
        updatedLots: List<CNEntity.Tender.Lot>,
        dividedLotId: String,
        params: DivideLotParams,
        updatedItems: List<CNEntity.Tender.Item>
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
                        value = lot.value.let { value ->
                            DivideLotResult.Tender.Lot.Value(amount = value.amount, currency = value.currency)
                        },
                        contractPeriod = lot.contractPeriod.let { contractPeriod ->
                            DivideLotResult.Tender.Lot.ContractPeriod(
                                startDate = contractPeriod.startDate,
                                endDate = contractPeriod.endDate
                            )
                        },
                        placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                            DivideLotResult.Tender.Lot.PlaceOfPerformance(
                                description = placeOfPerformance.description,
                                address = placeOfPerformance.address.let { address ->
                                    DivideLotResult.Tender.Lot.PlaceOfPerformance.Address(
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
                                                region = addressDetails.region.let { region ->
                                                    DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                        id = region.id,
                                                        description = region.description,
                                                        scheme = region.scheme,
                                                        uri = region.uri
                                                    )
                                                },
                                                locality = addressDetails.locality.let { locality ->
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
                                }
                            )
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

    private fun getUpdatedItems(
        params: DivideLotParams,
        newLotIdsByOldLotIds: Map<String, String>,
        tenderProcess: CNEntity
    ): List<CNEntity.Tender.Item> {
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
        dividedLotId: String
    ) = params.tender.lots
        .filter { lot -> lot.id != dividedLotId }
        .associateBy(
            keySelector = { lot -> lot.id },
            valueTransform = { lot -> generateLot(lot) }
        )

    private fun updateDividedLot(
        tenderProcess: CNEntity,
        dividedLotId: String
    ) = tenderProcess.tender.lots.map { lot ->
        if (lot.id == dividedLotId)
            lot.copy(status = LotStatus.CANCELLED, statusDetails = LotStatusDetails.EMPTY)
        else
            lot
    }

    private fun generateLot(lot: DivideLotParams.Tender.Lot) =
        CNEntity.Tender.Lot(
            id = generationService.lotId().toString(),
            status = LotStatus.ACTIVE,
            statusDetails = LotStatusDetails.EMPTY,
            internalId = lot.internalId,
            title = lot.title!!,
            description = lot.description!!,
            value = lot.value!!.let { value ->
                CNEntity.Tender.Lot.Value(amount = value.amount, currency = value.currency)
            },
            contractPeriod = lot.contractPeriod!!.let { contractPeriod ->
                CNEntity.Tender.Lot.ContractPeriod(
                    startDate = contractPeriod.startDate,
                    endDate = contractPeriod.endDate
                )
            },
            placeOfPerformance = lot.placeOfPerformance!!.let { placeOfPerformance ->
                CNEntity.Tender.Lot.PlaceOfPerformance(
                    description = placeOfPerformance.description,
                    address =  placeOfPerformance.address.let { address ->
                        CNEntity.Tender.Lot.PlaceOfPerformance.Address(
                            streetAddress = address.streetAddress,
                            postalCode = address.postalCode,
                            addressDetails = address.addressDetails.let { addressDetails ->
                                CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                    country = addressDetails.country.let { country ->
                                        CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                            id = country.id,
                                            description = country.description,
                                            scheme = country.scheme,
                                            uri = country.uri
                                        )
                                    },
                                    region = addressDetails.region.let { region ->
                                        CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                            id = region.id,
                                            description = region.description,
                                            scheme = region.scheme,
                                            uri = region.uri
                                        )
                                    },
                                    locality = addressDetails.locality.let { locality ->
                                        CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                            id = locality.id,
                                            description = locality.description,
                                            scheme = locality.scheme,
                                            uri = locality.uri
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            },
            hasOptions = false,
            options = emptyList(),
            hasRenewal = false,
            renewal = null,
            hasRecurrence = false,
            recurrence = null
        )

    fun getItemsByLots(context: GetItemsByLotsContext, data: GetItemsByLotsData): GetItemsByLotsResult {
        val tenderProcessEntity = tenderProcessDao.getByCpIdAndStage(context.cpid, context.stage)
            ?: throw ErrorException(DATA_NOT_FOUND, "Tender by '${context.cpid}' and stage ${context.stage} not found")

        val cn = toObject(CNEntity::class.java, tenderProcessEntity.jsonData)
        val receivedLotIds = data.lots.toSet { it.id }
        val itemsByLots = cn.tender.items.groupBy { it.relatedLot }

        val lotsWithoutRelatedItems = receivedLotIds - itemsByLots.keys

        if (lotsWithoutRelatedItems.isNotEmpty())
            throw ErrorException(
                ErrorType.RELATED_ITEMS_NOT_FOUND,
                "No items are linked via relatedLot to lot(s) '${lotsWithoutRelatedItems.joinToString()}' "
            )

        val itemsRelatedToReceivedLots = receivedLotIds
            .flatMap { lotId ->
                itemsByLots.getValue(lotId)
                    .map { item -> item.toGetItemsByLotsResultItem() }
            }.let { GetItemsByLotsResult(it) }

        return itemsRelatedToReceivedLots
    }

    private fun CNEntity.Tender.Item.toGetItemsByLotsResultItem() = GetItemsByLotsResult.Item(
        id = id,
        description = description,
        internalId = internalId,
        classification = classification.let { classification ->
            GetItemsByLotsResult.Item.Classification(
                id = classification.id,
                description = classification.description,
                scheme = classification.scheme
            )
        },
        additionalClassifications = additionalClassifications
            ?.map { additionalClassification ->
                GetItemsByLotsResult.Item.AdditionalClassification(
                    id = additionalClassification.id,
                    scheme = additionalClassification.scheme,
                    description = additionalClassification.description
                )
            },
        quantity = quantity,
        relatedLot = relatedLot,
        unit = unit.let { unit ->
            GetItemsByLotsResult.Item.Unit(
                id = unit.id,
                name = unit.name
            )
        }
    )
}