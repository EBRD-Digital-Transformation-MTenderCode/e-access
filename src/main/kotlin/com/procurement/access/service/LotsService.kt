package com.procurement.access.service

import com.procurement.access.application.model.context.GetLotsAuctionContext
import com.procurement.access.application.model.data.GetLotsAuctionResponseData
import com.procurement.access.application.model.params.CheckLotsStateParams
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
import com.procurement.access.lib.extension.toSet
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.ocds.asMoney
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

@Service
class LotsService(private val tenderProcessDao: TenderProcessDao,
                  private val tenderProcessRepository: TenderProcessRepository,
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

    fun checkLotsState(params: CheckLotsStateParams): ValidationResult<Fail> {
        val tenderProcessEntity = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return ValidationErrors.TenderNotFoundOnCheckLotsState(cpid = params.cpid, ocid = params.ocid)
                .asValidationFailure()

        val tender = tenderProcessEntity.jsonData
            .tryToObject(TenderLotsInfo::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
            .onFailure { return it.reason.asValidationFailure() }

        val storedLotsById = tender.lots.orEmpty().associateBy { it.id }
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

    private fun checkLotState(lot: TenderLotsInfo.Lot, validStates: LotStatesRule): ValidationResult<ValidationErrors> =
        if (lotStateIsValid(lot, validStates))
            ValidationResult.ok()
        else ValidationErrors.InvalidLotState(lot.id).asValidationFailure()


    private fun lotStateIsValid(storedLot: TenderLotsInfo.Lot, validStates: LotStatesRule): Boolean =
        validStates.any { validState -> storedLot.status == validState.status
            && storedLot.statusDetails == validState.statusDetails }

}