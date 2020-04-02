package com.procurement.access.application.service.lot

import com.procurement.access.application.model.params.SetStateForLotsParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.BadRequestErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.lot.TemporalLotId
import com.procurement.access.domain.model.lot.tryCreateLotId
import com.procurement.access.domain.model.money.Money
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asFailure
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.domain.util.extension.getUnknownElements
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.get.lotStateByIds.GetLotStateByIdsResult
import com.procurement.access.infrastructure.handler.set.stateforlots.SetStateForLotsResult
import com.procurement.access.lib.orThrow
import com.procurement.access.lib.toSetBy
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
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

    fun getLotIds(
        cpId: Cpid,
        stage: Stage,
        states: List<GetLotIdsParams.State>
    ): Result<List<LotId>, Fail>

    fun getLotStateByIds(params: GetLotStateByIdsParams): Result<List<GetLotStateByIdsResult>, Fail>

    fun setStateForLots(params: SetStateForLotsParams): Result<List<SetStateForLotsResult>, Fail>
}

@Service
class LotServiceImpl(
    private val tenderProcessDao: TenderProcessDao,
    private val tenderProcessRepository: TenderProcessRepository
) : LotService {

    override fun setStateForLots(params: SetStateForLotsParams): Result<List<SetStateForLotsResult>, Fail> {
        val tenderProcessEntity = getTenderProcessEntityByCpIdAndStage(cpId = params.cpid, stage = params.ocid.stage)
            .doOnError { error -> return Result.failure(error) }
            .get

        val tenderProcess = tenderProcessEntity.jsonData
            .tryToObject(TenderProcess::class.java)
            .doOnError { error -> return Result.failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }
            .get

        val lotsIds = params.lots
            .map { it.id.toString() }
            .toSetBy { it }
        val dbLotsIds: Set<String> = tenderProcess.tender.lots
            .map { it.id }
            .toSetBy { it }
        val unknownLotsIds = getUnknownElements(received = lotsIds, known = dbLotsIds)
        if (unknownLotsIds.isNotEmpty()) {
            return Result.failure(ValidationErrors.LotsNotFound(lotsId = unknownLotsIds))
        }

        val mapRequestIds = params.lots
            .associateBy { it.id.toString() }

        val updatedLots = tenderProcess.tender.lots
            .map { dbLot ->
                mapRequestIds[dbLot.id]
                    ?.let {
                        dbLot.setStatusAndStatusDetails(requestLot = it)
                    }
                    ?: dbLot
            }
        val updatedTenderProcess = tenderProcess.copy(
            tender = tenderProcess.tender.copy(
                lots = updatedLots
            )
        )
        tenderProcessRepository.save(tenderProcessEntity.updateTenderProcess(tenderProcess = updatedTenderProcess))
        return params.lots
            .map { lot ->
                SetStateForLotsResult(
                    id = lot.id,
                    status = lot.status,
                    statusDetails = lot.statusDetails
                )
            }
            .asSuccess()
    }

    override fun getLotStateByIds(params: GetLotStateByIdsParams): Result<List<GetLotStateByIdsResult>, Fail> {

        val tenderProcess = getTenderProcessEntityByCpIdAndStage(cpId = params.cpid, stage = params.ocid.stage)
            .doOnError { error -> return Result.failure(error) }
            .get
            .jsonData
            .tryToObject(TenderProcess::class.java)
            .doOnError { error -> return Result.failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }
            .get

        if (params.lotIds.isEmpty()) {
            return tenderProcess.tender.lots.map { lot ->
                lot.convertToGetLotStateByIdsResult()
                    .doOnError { error -> return error.asFailure() }
                    .get
            }.asSuccess()
        }

        val receivedLotIds = params.lotIds.toSetBy { it.toString() }

        val filteredLots = tenderProcess.tender.lots.filter { lot -> receivedLotIds.contains(lot.id) }

        val knownLots = filteredLots.toSetBy { it.id }
        val unknownLots = receivedLotIds - knownLots
        if (unknownLots.isNotEmpty())
            return ValidationErrors.LotsNotFound(unknownLots)
                .asFailure()

        return filteredLots.map {
                it.convertToGetLotStateByIdsResult()
                    .doOnError { error -> return error.asFailure() }
                    .get
            }
            .asSuccess()
    }

    override fun getLotIds(
        cpId: Cpid,
        stage: Stage,
        states: List<GetLotIdsParams.State>
    ): Result<List<LotId>, Fail> {

        val data = getTenderProcessEntityByCpIdAndStage(cpId = cpId, stage = stage)
            .doOnError { error -> return Result.failure(error) }
            .get
            .jsonData

        val tenderProcess = data.tryToObject(TenderProcess::class.java)
            .doOnError { error -> return Result.failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }
            .get


        return when {
            states.isEmpty() ->
                Result.success(
                    tenderProcess.tender.lots
                        .map { lot -> LotId.fromString(lot.id) }
                )
            else -> {
                val sortedStatuses = states.sorted()
                Result.success(
                    getLotsOnStates(
                        lots = tenderProcess.tender.lots,
                        states = sortedStatuses
                    ).map { lot -> LotId.fromString(lot.id) })
            }
        }
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

            OperationType.CREATE_CN,
            OperationType.CREATE_PN,
            OperationType.CREATE_PIN,
            OperationType.UPDATE_PN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN ->
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
        val entity = tenderProcessDao.getByCpIdAndStage(context.cpid, context.stage)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)

        val cn: CNEntity = toObject(CNEntity::class.java, entity.jsonData)

        val idsUnsuccessfulLots: Set<LotId> = data.lots.toSetBy { it.id }
        val updatedLots: List<CNEntity.Tender.Lot> = cn.tender.lots.setUnsuccessfulStatus(ids = idsUnsuccessfulLots)
        val activeLotsIsPresent = updatedLots.any { it.status == LotStatus.ACTIVE }

        val updatedCN = cn.copy(
            tender = cn.tender.copy(
                status = if (activeLotsIsPresent) cn.tender.status else TenderStatus.UNSUCCESSFUL,
                statusDetails = if (activeLotsIsPresent) cn.tender.statusDetails else TenderStatusDetails.EMPTY,
                lots = updatedLots
            )
        )

        tenderProcessDao.save(
            TenderProcessEntity(
                cpId = context.cpid,
                token = entity.token,
                stage = context.stage,
                owner = entity.owner,
                createdDate = context.startDate.toDate(),
                jsonData = toJson(updatedCN)
            )
        )

        return SettedLotsStatusUnsuccessful(
            tender = SettedLotsStatusUnsuccessful.Tender(
                status = updatedCN.tender.status,
                statusDetails = updatedCN.tender.statusDetails
            ),
            lots = data.lots.map { lot ->
                SettedLotsStatusUnsuccessful.Lot(
                    id = lot.id,
                    status = LotStatus.UNSUCCESSFUL
                )
            }
        )
    }

    private fun Lot.convertToGetLotStateByIdsResult(): Result<GetLotStateByIdsResult, Fail> {

        val lotId = this.id.tryCreateLotId()
            .doOnError { error ->
                return Fail.Incident.DatabaseIncident(exception = error.exception)
                    .asFailure()
            }
            .get

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
        val idsNewLots: Set<TemporalLotId> = getNewElements(receivedLotsIds, savedLotsIds)
        val idsUpdatingLots: Set<TemporalLotId> = getElementsForUpdate(receivedLotsIds, savedLotsIds)

        val newLots = getNewLots(idsNewLots, receivedLotsByIds)
        val updatingLots = getUpdatingActiveLots(idsUpdatingLots, savedLotsByIds)

        return LotsForAuction(lots = (newLots + updatingLots).toList())
    }

    private fun getNewLots(
        idsNewLots: Set<TemporalLotId>,
        receivedLotsByIds: Map<TemporalLotId, LotsForAuctionData.Lot>
    ): Sequence<LotsForAuction.Lot> = idsNewLots.asSequence()
        .map { id ->
            receivedLotsByIds.getValue(id)
                .let { lot ->
                    LotsForAuction.Lot(
                        id = id,
                        value = lot.value
                    )
                }
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

    fun <T> getNewElements(received: Set<T>, saved: Set<T>) = received.subtract(saved)

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

    private fun getTenderProcessEntityByCpIdAndStage(cpId: Cpid, stage: Stage): Result<TenderProcessEntity, Fail> {
        val entity = tenderProcessRepository.getByCpIdAndStage(cpid = cpId, stage = stage)
            .doOnError { error -> return Result.failure(error) }
            .get
            ?: return Result.failure(
                BadRequestErrors.EntityNotFound(
                    entityName = "TenderProcessEntity",
                    by = "by cpid = '$cpId' and stage = '$stage'"
                )
            )

        return Result.success(entity)
    }

    private fun getLotsOnStates(lots: List<Lot>, states: List<GetLotIdsParams.State>): List<Lot> {
        return lots.filter { lot ->
            val state = states.firstOrNull { state ->
                when {
                    state.status == null -> lot.statusDetails == state.statusDetails
                    state.statusDetails == null -> lot.status == state.status
                    else -> lot.statusDetails == state.statusDetails && lot.status == state.status
                }
            }
            state != null
        }
    }

    private fun Lot.setStatusAndStatusDetails(requestLot: SetStateForLotsParams.Lot): Lot {
        return this.copy(status = requestLot.status, statusDetails = requestLot.statusDetails)
    }

    private fun TenderProcessEntity.updateTenderProcess(tenderProcess: TenderProcess): TenderProcessEntity {
        return this.copy(jsonData = toJson(tenderProcess))
    }
}
