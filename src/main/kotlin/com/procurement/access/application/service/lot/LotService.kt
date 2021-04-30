package com.procurement.access.application.service.lot

import com.procurement.access.application.model.params.SetStateForLotsParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.lot.TemporalLotId
import com.procurement.access.domain.model.lot.tryCreateLotId
import com.procurement.access.domain.model.money.Money
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.entity.RfqEntity
import com.procurement.access.infrastructure.handler.v1.converter.convertToSetStateForLotsResult
import com.procurement.access.infrastructure.handler.v2.model.response.GetLotStateByIdsResult
import com.procurement.access.infrastructure.handler.v2.model.response.SetStateForLotsResult
import com.procurement.access.lib.extension.getUnknownElements
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.extension.orThrow
import com.procurement.access.lib.extension.toSet
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.success
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface LotService {
    fun getLot(context: GetLotContext): GettedLot

    fun getLotsForAuction(context: LotsForAuctionContext, data: LotsForAuctionData): LotsForAuction

    fun setStatusUnsuccessful(
        context: SetLotsStatusUnsuccessfulContext,
        data: SetLotsStatusUnsuccessfulData
    ): SettedLotsStatusUnsuccessful

    fun findLotIds(params: FindLotIdsParams): Result<List<LotId>, Fail>

    fun getLotStateByIds(params: GetLotStateByIdsParams): Result<List<GetLotStateByIdsResult>, Fail>

    fun setStateForLots(params: SetStateForLotsParams): Result<List<SetStateForLotsResult>, Fail>
}

@Service
class LotServiceImpl(
    private val tenderProcessDao: TenderProcessDao,
    private val tenderProcessRepository: TenderProcessRepository
) : LotService {

    override fun setStateForLots(params: SetStateForLotsParams): Result<List<SetStateForLotsResult>, Fail> {
        val tenderProcessEntity = tenderProcessRepository
            .getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage)
            .onFailure { return it }
            ?: return Result.failure(
                ValidationErrors.LotsNotFoundSetStateForLots(lotsId = params.lots.map { it.id.toString() })
            )

        val receivedLotsIds = params.lots
            .toSet { it.id.toString() }

        val result = when (params.ocid.stage) {
            Stage.EV,
            Stage.NP,
            Stage.TP -> {
                val cn = tenderProcessEntity.jsonData
                    .tryToObject(CNEntity::class.java)
                    .mapFailure {
                        Fail.Incident.DatabaseIncident(exception = it.exception)
                    }
                    .onFailure { return it }

                val dbLotsIds: Set<String> = cn.tender.lots
                    .toSet { it.id }

                val unknownLotsIds = getUnknownElements(received = receivedLotsIds, known = dbLotsIds)
                if (unknownLotsIds.isNotEmpty()) {
                    return Result.failure(ValidationErrors.LotsNotFoundSetStateForLots(lotsId = unknownLotsIds))
                }

                val mapRequestIds = params.lots
                    .associateBy { it.id.toString() }

                val resultLots = mutableListOf<CNEntity.Tender.Lot>()

                val updatedLots = cn.tender.lots
                    .map { dbLot ->
                        mapRequestIds[dbLot.id]
                            ?.let { requestLot ->
                                val dbLotState = LotStateInfo(dbLot.status, dbLot.statusDetails)
                                if (statusOrStatusDetailsVaries(
                                        databaseLotStateInfo = dbLotState,
                                        requestLot = requestLot
                                    )) {
                                    val updatedLot = dbLot.copy(
                                        status = requestLot.status,
                                        statusDetails = requestLot.statusDetails ?: dbLot.statusDetails
                                    )
                                    resultLots.add(updatedLot)
                                    updatedLot
                                } else dbLot
                            }
                            ?: dbLot
                    }
                val updatedCNEntity = cn.copy(
                    tender = cn.tender.copy(
                        lots = updatedLots
                    )
                )

                val updatedTenderProcessEntity = tenderProcessEntity.copy(jsonData = toJson(updatedCNEntity))

                tenderProcessRepository.save(updatedTenderProcessEntity)
                    .onFailure { incident -> return incident }

                resultLots.toList().mapResult { it.convertToSetStateForLotsResult() }
            }

            Stage.AP -> {
                val ap = tenderProcessEntity.jsonData
                    .tryToObject(APEntity::class.java)
                    .mapFailure {
                        Fail.Incident.DatabaseIncident(exception = it.exception)
                    }
                    .onFailure { return it }

                val storedLots = ap.tender.lots.orEmpty()

                val dbLotsIds: Set<String> = storedLots
                    .toSet { it.id }

                val unknownLotsIds = getUnknownElements(received = receivedLotsIds, known = dbLotsIds)
                if (unknownLotsIds.isNotEmpty()) {
                    return Result.failure(ValidationErrors.LotsNotFoundSetStateForLots(lotsId = unknownLotsIds))
                }

                val mapRequestIds = params.lots
                    .associateBy { it.id.toString() }

                val resultLots = mutableListOf<APEntity.Tender.Lot>()

                val updatedLots = storedLots
                    .map { dbLot ->
                        mapRequestIds[dbLot.id]
                            ?.let { requestLot ->
                                val dbLotState = LotStateInfo(dbLot.status, dbLot.statusDetails)
                                if (statusOrStatusDetailsVaries(
                                        databaseLotStateInfo = dbLotState,
                                        requestLot = requestLot
                                    )) {
                                    val updatedLot = dbLot.copy(
                                        status = requestLot.status,
                                        statusDetails = requestLot.statusDetails ?: dbLot.statusDetails
                                    )
                                    resultLots.add(updatedLot)
                                    updatedLot
                                } else dbLot
                            }
                            ?: dbLot
                    }
                val updatedAPEntity = ap.copy(
                    tender = ap.tender.copy(
                        lots = updatedLots
                    )
                )

                val updatedTenderProcessEntity = tenderProcessEntity.copy(jsonData = toJson(updatedAPEntity))

                tenderProcessRepository.save(updatedTenderProcessEntity)
                    .onFailure { incident -> return incident }

                resultLots.toList().mapResult { it.convertToSetStateForLotsResult() }
            }

            Stage.PN -> {
                val pn = tenderProcessEntity.jsonData
                    .tryToObject(PNEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val dbLotsIds: Set<String> = pn.tender.lots
                    .toSet { it.id }

                val unknownLotsIds = getUnknownElements(received = receivedLotsIds, known = dbLotsIds)
                if (unknownLotsIds.isNotEmpty()) {
                    return Result.failure(ValidationErrors.LotsNotFoundSetStateForLots(lotsId = unknownLotsIds))
                }

                val mapRequestIds = params.lots
                    .associateBy { it.id.toString() }

                val resultLots = mutableListOf<PNEntity.Tender.Lot>()

                val updatedLots = pn.tender.lots
                    .map { dbLot ->
                        mapRequestIds[dbLot.id]
                            ?.let { requestLot ->
                                val dbLotState = LotStateInfo(dbLot.status, dbLot.statusDetails)
                                if (statusOrStatusDetailsVaries(
                                        databaseLotStateInfo = dbLotState,
                                        requestLot = requestLot
                                    )) {
                                    val updatedLot = dbLot.copy(
                                        status = requestLot.status,
                                        statusDetails = requestLot.statusDetails ?: dbLot.statusDetails
                                    )
                                    resultLots.add(updatedLot)
                                    updatedLot
                                } else dbLot
                            }
                            ?: dbLot
                    }
                val updatedPNEntity = pn.copy(
                    tender = pn.tender.copy(
                        lots = updatedLots
                    )
                )

                val updatedTenderProcessEntity = tenderProcessEntity.copy(jsonData = toJson(updatedPNEntity))

                tenderProcessRepository.save(updatedTenderProcessEntity)
                    .onFailure { incident -> return incident }

                resultLots.toList().mapResult { it.convertToSetStateForLotsResult() }
            }

            Stage.AC,
            Stage.EI,
            Stage.FE,
            Stage.FS,
            Stage.PC,
            Stage.RQ ->
                Result.failure(
                    ValidationErrors.UnexpectedStageForSetStateForLots(stage = params.ocid.stage)
                )
        }
            .onFailure { error -> return error }

        return success(result)
    }

    override fun getLotStateByIds(params: GetLotStateByIdsParams): Result<List<GetLotStateByIdsResult>, Fail> {

        val tenderProcessEntity = tenderProcessRepository.getByCpIdAndStage(
            cpid = params.cpid,
            stage = params.ocid.stage
        )
            .onFailure { return it }
            ?: return Result.failure(
                ValidationErrors.TenderNotFoundGetLotStateByIds(cpid = params.cpid, ocid = params.ocid)
            )

        val tenderProcess = tenderProcessEntity.jsonData
            .tryToObject(TenderProcess::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
            .onFailure { return it }

        val receivedLotIds = params.lotIds.toSet { it.toString() }

        val filteredLots = tenderProcess.tender.lots.filter { lot -> receivedLotIds.contains(lot.id) }

        val knownLots = filteredLots.toSet { it.id }
        val unknownLots = receivedLotIds - knownLots
        if (unknownLots.isNotEmpty())
            return ValidationErrors.LotsNotFoundGetLotStateByIds(unknownLots)
                .asFailure()

        return filteredLots.map {
            it.convertToGetLotStateByIdsResult()
                .onFailure { return it }
        }
            .asSuccess()
    }

    override fun findLotIds(params: FindLotIdsParams): Result<List<LotId>, Fail> {
        val stage = params.ocid.stage

        val tenderProcessEntity = tenderProcessRepository
            .getByCpIdAndStage(cpid = params.cpid, stage = stage)
            .onFailure { error -> return error }
            ?: return emptyList<LotId>().asSuccess()

        val result = when (stage) {
            Stage.EV,
            Stage.NP,
            Stage.TP -> {
                val cn = tenderProcessEntity.jsonData
                    .tryToObject(CNEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val lotIds = when {
                    params.states.isEmpty() -> cn.tender.lots
                        .map { lot -> LotId.fromString(lot.id) }

                    else -> {
                        params.states
                            .let { sortedStatuses -> getLotsOnStates(lots = cn.tender.lots, states = sortedStatuses) }
                            .map { lot -> LotId.fromString(lot.id) }
                    }
                }
                success(lotIds)
            }

            Stage.AP -> {
                val ap = tenderProcessEntity.jsonData
                    .tryToObject(APEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val lots = ap.tender.lots.orEmpty()

                val lotIds = when {
                    params.states.isEmpty() -> lots
                        .map { lot -> LotId.fromString(lot.id) }

                    else -> {
                        params.states
                            .let { sortedStatuses -> getAPLotsOnStates(lots = lots, states = sortedStatuses) }
                            .map { lot -> LotId.fromString(lot.id) }
                    }
                }

                success(lotIds)
            }

            Stage.PN -> {
                val pn = tenderProcessEntity.jsonData
                    .tryToObject(PNEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val lotIds = when {
                    params.states.isEmpty() -> pn.tender.lots
                        .map { lot -> LotId.fromString(lot.id) }

                    else -> {
                        params.states
                            .let { sortedStatuses -> getPNLotsOnStates(lots = pn.tender.lots, states = sortedStatuses) }
                            .map { lot -> LotId.fromString(lot.id) }
                    }
                }

                success(lotIds)
            }

            Stage.RQ -> {
                val rfq = tenderProcessEntity.jsonData
                    .tryToObject(RfqEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val lotIds = when {
                    params.states.isEmpty() -> rfq.tender.lots
                        .map { lot -> lot.id }

                    else -> params.states
                            .let { sortedStatuses -> getRfqLotsOnStates(lots = rfq.tender.lots, states = sortedStatuses) }
                            .map { lot -> lot.id }
                }

                success(lotIds)
            }

            Stage.FE -> success(emptyList())

            Stage.AC,
            Stage.EI,
            Stage.FS,
            Stage.PC ->
                Result.failure(
                    ValidationErrors.UnexpectedStageForFindLotIds(stage = params.ocid.stage)
                )
        }
            .onFailure { error -> return error }

        return success(result)
    }

    override fun getLot(context: GetLotContext): GettedLot {
        val entity = tenderProcessDao.getByCpIdAndStage(context.cpid, context.stage)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)

        return toObject(CNEntity::class.java, entity.jsonData)
            .tender
            .lots
            .find { lot ->
                LotId.fromString(lot.id) == context.lotId
            }
            ?.let { lot ->
                GettedLot(
                    id = lot.id,
                    internalId = lot.internalId,
                    title = lot.title,
                    description = lot.description,
                    status = lot.status,
                    statusDetails = lot.statusDetails,
                    value = Money(amount = lot.value.amount, currency = lot.value.currency),
                    options = lot.options.map { option ->
                        GettedLot.Option(
                            hasOptions = option.hasOptions
                        )
                    },
                    variants = lot.variants.map { variant ->
                        GettedLot.Variant(
                            hasVariants = variant.hasVariants
                        )
                    },
                    renewals = lot.renewals.map { renewal ->
                        GettedLot.Renewal(
                            hasRenewals = renewal.hasRenewals
                        )
                    },
                    recurrentProcurement = lot.recurrentProcurement.map { recurrentProcurement ->
                        GettedLot.RecurrentProcurement(
                            isRecurrent = recurrentProcurement.isRecurrent
                        )
                    },
                    contractPeriod = lot.contractPeriod.let { contractPeriod ->
                        GettedLot.ContractPeriod(
                            startDate = contractPeriod.startDate,
                            endDate = contractPeriod.endDate
                        )
                    },
                    placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                        GettedLot.PlaceOfPerformance(
                            description = placeOfPerformance.description,
                            address = placeOfPerformance.address.let { address ->
                                GettedLot.PlaceOfPerformance.Address(
                                    streetAddress = address.streetAddress,
                                    postalCode = address.postalCode,
                                    addressDetails = address.addressDetails.let { addressDetails ->
                                        GettedLot.PlaceOfPerformance.Address.AddressDetails(
                                            country = addressDetails.country.let { country ->
                                                GettedLot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                    scheme = country.scheme,
                                                    id = country.id,
                                                    description = country.description,
                                                    uri = country.uri
                                                )
                                            },
                                            region = addressDetails.region.let { region ->
                                                GettedLot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                    scheme = region.scheme,
                                                    id = region.id,
                                                    description = region.description,
                                                    uri = region.uri
                                                )
                                            },
                                            locality = addressDetails.locality.let { locality ->
                                                GettedLot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
            .orThrow {
                ErrorException(
                    error = ErrorType.LOT_NOT_FOUND,
                    message = "In tender by cpid '${context.cpid}' and stage '${context.stage}' the lot by id '${context.lotId}' not found."
                )
            }
    }

    override fun getLotsForAuction(context: LotsForAuctionContext, data: LotsForAuctionData): LotsForAuction {
        return when (context.operationType) {
            OperationType.CREATE_CN_ON_PN -> getLotsForCnOnPn(context, data)

            OperationType.UPDATE_CN -> getLotsForUpdateCn(context, data)

            OperationType.AMEND_FE,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.AWARD_CONSIDERATION,
            OperationType.COMPLETE_QUALIFICATION,
            OperationType.CREATE_AWARD,
            OperationType.CREATE_CN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
            OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE,
            OperationType.CREATE_FE,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN,
            OperationType.CREATE_PCR,
            OperationType.CREATE_PIN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_PN,
            OperationType.CREATE_RFQ,
            OperationType.CREATE_SUBMISSION,
            OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.DIVIDE_LOT,
            OperationType.ISSUING_FRAMEWORK_CONTRACT,
            OperationType.OUTSOURCING_PN,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.QUALIFICATION_PROTOCOL,
            OperationType.RELATION_AP,
            OperationType.START_SECONDSTAGE,
            OperationType.SUBMISSION_PERIOD_END,
            OperationType.SUBMIT_BID,
            OperationType.TENDER_PERIOD_END,
            OperationType.UPDATE_AP,
            OperationType.UPDATE_AWARD,
            OperationType.UPDATE_PN,
            OperationType.WITHDRAW_BID,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL ->
                throw ErrorException(
                    error = ErrorType.INVALID_OPERATION_TYPE,
                    message = "The 'getLotsForAuction' command does not apply for '${context.operationType.key}' operation type."
                )
        }
    }

    override fun setStatusUnsuccessful(
        context: SetLotsStatusUnsuccessfulContext,
        data: SetLotsStatusUnsuccessfulData
    ): SettedLotsStatusUnsuccessful {
        val entity = tenderProcessDao.getByCpIdAndStage(context.cpid, context.stage.key)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)

        val idsUnsuccessfulLots = data.lots.toSet { it.id.toString() }

        val (tenderJson, result) = when (context.stage) {
            Stage.AC,
            Stage.EV,
            Stage.FE,
            Stage.NP,
            Stage.TP -> {
                val cn = toObject(CNEntity::class.java, entity.jsonData)
                val updatedLots: List<CNEntity.Tender.Lot> = cn.tender.lots.map { lot ->
                    if (lot.id in idsUnsuccessfulLots)
                        lot.copy(status = LotStatus.UNSUCCESSFUL)
                    else
                        lot
                }
                val activeLotsIsPresent = updatedLots.any { it.status == LotStatus.ACTIVE }

                val updatedCN = cn.copy(
                    tender = cn.tender.copy(
                        status = if (activeLotsIsPresent) cn.tender.status else TenderStatus.UNSUCCESSFUL,
                        statusDetails = if (activeLotsIsPresent) cn.tender.statusDetails else TenderStatusDetails.EMPTY,
                        lots = updatedLots
                    )
                )

                toJson(updatedCN) to SettedLotsStatusUnsuccessful.fromDomain(updatedCN, data)
            }

            Stage.RQ -> {
                val rq = toObject(RfqEntity::class.java, entity.jsonData)
                val updatedLots: List<RfqEntity.Tender.Lot> = rq.tender.lots.map { lot ->
                    if (lot.id.toString() in idsUnsuccessfulLots)
                        lot.copy(status = LotStatus.UNSUCCESSFUL)
                    else
                        lot
                }
                val activeLotsIsPresent = updatedLots.any { it.status == LotStatus.ACTIVE }

                val updatedRfq = rq.copy(
                    tender = rq.tender.copy(
                        status = if (activeLotsIsPresent) rq.tender.status else TenderStatus.UNSUCCESSFUL,
                        statusDetails = if (activeLotsIsPresent) rq.tender.statusDetails else TenderStatusDetails.EMPTY,
                        lots = updatedLots
                    )
                )

                toJson(updatedRfq) to SettedLotsStatusUnsuccessful.fromDomain(updatedRfq, data)
            }

            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PN -> throw ErrorException(
                error = ErrorType.INVALID_STAGE,
                message = "Stage ${context.stage} not allowed at the command."
            )
        }

        tenderProcessDao.save(
            TenderProcessEntity(
                cpId = context.cpid,
                token = entity.token,
                stage = context.stage.key,
                owner = entity.owner,
                createdDate = context.startDate,
                jsonData = tenderJson
            )
        )

        return result
    }

    private fun Lot.convertToGetLotStateByIdsResult(): Result<GetLotStateByIdsResult, Fail> {

        val lotId = this.id.tryCreateLotId()
            .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
            .onFailure { return it }

        return GetLotStateByIdsResult(
            id = lotId,
            status = this.status!!,
            statusDetails = this.statusDetails!!
        )
            .asSuccess()
    }

    private fun getLotsForCnOnPn(context: LotsForAuctionContext, data: LotsForAuctionData): LotsForAuction =
        tenderProcessDao.getByCpIdAndStage(context.cpid, context.prevStage)
            ?.let { entity ->
                val process = toObject(TenderProcess::class.java, entity.jsonData)
                getLotFromTender(lots = process.tender.lots)
                    .takeIf {
                        it.lots.isNotEmpty()
                    }
                    ?: getLotFromRequest(lots = data.lots)
            }
            ?: getLotFromRequest(lots = data.lots)

    private fun getLotFromRequest(lots: List<LotsForAuctionData.Lot>): LotsForAuction = LotsForAuction(
        lots = lots.map { lot ->
            LotsForAuction.Lot(
                id = lot.id,
                value = lot.value
            )
        }
    )

    private fun getLotFromTender(lots: List<Lot>): LotsForAuction = LotsForAuction(
        lots = lots.asSequence()
            .filter { it.status == LotStatus.PLANNING }
            .map { lot ->
                LotsForAuction.Lot(
                    id = lot.id,
                    value = lot.value.let { value ->
                        Money(
                            amount = value.amount,
                            currency = value.currency
                        )
                    }
                )
            }
            .toList()
    )

    private fun getLotsForUpdateCn(context: LotsForAuctionContext, data: LotsForAuctionData): LotsForAuction {
        val receivedLotsByIds: Map<TemporalLotId, LotsForAuctionData.Lot> = data.lots.associateBy { it.id }
        val savedLotsByIds: Map<String, CNEntity.Tender.Lot> =
            tenderProcessDao.getByCpIdAndStage(context.cpid, context.prevStage)
                ?.let { entity ->
                    val cn = toObject(CNEntity::class.java, entity.jsonData)
                    cn.tender.lots
                        .associateBy { lot ->
                            lot.id
                        }
                }
                ?: emptyMap()

        val receivedLotsIds = receivedLotsByIds.keys
        val savedLotsIds = savedLotsByIds.keys
        val idsUpdatingLots: Set<TemporalLotId> = getElementsForUpdate(receivedLotsIds, savedLotsIds)
        val updatingLots = getUpdatingActiveLots(idsUpdatingLots, savedLotsByIds).toList()
        return LotsForAuction(lots = updatingLots)
    }

    private fun getUpdatingActiveLots(
        idsUpdatingLots: Set<TemporalLotId>,
        savedLotsByIds: Map<String, CNEntity.Tender.Lot>
    ): Sequence<LotsForAuction.Lot> = idsUpdatingLots.asSequence()
        .map { id ->
            savedLotsByIds.getValue(id)
        }
        .filter { lot ->
            lot.status == LotStatus.ACTIVE
        }
        .map { lot ->
            LotsForAuction.Lot(
                id = lot.id,
                value = Money(
                    amount = lot.value.amount,
                    currency = lot.value.currency
                )
            )
        }

    private fun <T> getElementsForUpdate(received: Set<T>, saved: Set<T>) = saved.intersect(received)

    private fun List<CNEntity.Tender.Lot>.setUnsuccessfulStatus(
        ids: Set<LotId>
    ) = this.map { lot ->
        if (LotId.fromString(lot.id) in ids) {
            lot.copy(
                status = LotStatus.UNSUCCESSFUL
            )
        } else
            lot
    }

    private fun FindLotIdsParams.State.matchesWith(lotStatus: LotStatus, lotStatusDetails: LotStatusDetails) =
        when {
            status == null && statusDetails != null -> lotStatusDetails == statusDetails
            status != null && statusDetails == null -> lotStatus == status
            status != null && statusDetails != null -> lotStatus == status && lotStatusDetails == statusDetails
            else -> throw IllegalArgumentException("State must contains 'status' or/and 'statusDetails'. Missing 'state' and 'statusDetails'")
        }

    private fun getLotsOnStates(
        lots: List<CNEntity.Tender.Lot>,
        states: List<FindLotIdsParams.State>
    ): List<CNEntity.Tender.Lot> {
        val sortedStates = states.sorted()
        return lots.filter { lot ->
            val foundedState = sortedStates.find { state -> state.matchesWith(lot.status, lot.statusDetails) }
            foundedState != null
        }
    }

    private fun getAPLotsOnStates(
        lots: List<APEntity.Tender.Lot>,
        states: List<FindLotIdsParams.State>
    ): List<APEntity.Tender.Lot> {
        val sortedStates = states.sorted()
        return lots.filter { lot ->
            val foundedState = sortedStates.find { state -> state.matchesWith(lot.status, lot.statusDetails) }
            foundedState != null
        }
    }

    private fun getPNLotsOnStates(
        lots: List<PNEntity.Tender.Lot>,
        states: List<FindLotIdsParams.State>
    ): List<PNEntity.Tender.Lot> {
        val sortedStates = states.sorted()
        return lots.filter { lot ->
            val foundedState = sortedStates.find { state -> state.matchesWith(lot.status, lot.statusDetails) }
            foundedState != null
        }
    }

    private fun getRfqLotsOnStates(
        lots: List<RfqEntity.Tender.Lot>,
        states: List<FindLotIdsParams.State>
    ): List<RfqEntity.Tender.Lot> {
        val sortedStates = states.sorted()
        return lots.filter { lot ->
            val foundedState = sortedStates.find { state -> state.matchesWith(lot.status, lot.statusDetails) }
            foundedState != null
        }
    }

    private data class LotStateInfo(
        val status: LotStatus,
        val statusDetails: LotStatusDetails
    )

    private fun statusOrStatusDetailsVaries(
        databaseLotStateInfo: LotStateInfo, requestLot: SetStateForLotsParams.Lot
    ) = databaseLotStateInfo.status != requestLot.status || databaseLotStateInfo.statusDetails != requestLot.statusDetails
}
