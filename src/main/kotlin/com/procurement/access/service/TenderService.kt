package com.procurement.access.service

import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.tender.strategy.get.state.GetTenderStateParams
import com.procurement.access.application.service.tender.strategy.get.state.GetTenderStateResult
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asFailure
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.CONTEXT
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.exception.ErrorType.INVALID_LOTS_STATUS
import com.procurement.access.exception.ErrorType.INVALID_OPERATION_TYPE
import com.procurement.access.exception.ErrorType.INVALID_OWNER
import com.procurement.access.exception.ErrorType.INVALID_TOKEN
import com.procurement.access.exception.ErrorType.IS_NOT_SUSPENDED
import com.procurement.access.exception.ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.ResponseDto
import com.procurement.access.model.dto.lots.CancellationRs
import com.procurement.access.model.dto.lots.LotCancellation
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.tender.GetDataForAcRq
import com.procurement.access.model.dto.tender.GetDataForAcRs
import com.procurement.access.model.dto.tender.GetDataForAcTender
import com.procurement.access.model.dto.tender.GetTenderOwnerRs
import com.procurement.access.model.dto.tender.UnsuspendedTender
import com.procurement.access.model.dto.tender.UnsuspendedTenderRs
import com.procurement.access.model.dto.tender.UpdateTenderStatusRs
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.localNowUTC
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

@Service
class TenderService(
    private val tenderProcessDao: TenderProcessDao,
    private val generationService: GenerationService,
    private val tenderProcessRepository: TenderProcessRepository
) {

    fun setSuspended(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.statusDetails = TenderStatusDetails.SUSPENDED
        tenderProcessDao.save(getEntity(process, entity))
        return ResponseDto(data = UpdateTenderStatusRs(
                process.tender.status.key,
                process.tender.statusDetails.key))
    }

    fun setUnsuspended(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val phase = cm.context.phase ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        if (process.tender.statusDetails == TenderStatusDetails.SUSPENDED) {
            process.tender.statusDetails = TenderStatusDetails.creator(phase)
        } else {
            throw ErrorException(IS_NOT_SUSPENDED)
        }
        tenderProcessDao.save(getEntity(process, entity))
        return ResponseDto(data = UnsuspendedTenderRs(UnsuspendedTender(
                process.tender.status.key,
                process.tender.statusDetails.key,
                process.tender.procurementMethodModalities,
                process.tender.electronicAuctions)))
    }

    fun setPreCancellation(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
        val token = cm.context.token ?: throw ErrorException(CONTEXT)
        val operationType = cm.context.operationType ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(INVALID_TOKEN)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        validateTenderStatusForPrepareCancellation(process, operationType)
        val lotStatusPredicate = getLotStatusPredicateForPrepareCancellation(operationType)
        val lotsResponseDto = mutableListOf<LotCancellation>()
        process.tender.apply {
            statusDetails = TenderStatusDetails.CANCELLATION
            lots.asSequence()
                    .filter(lotStatusPredicate)
                    .forEach { lot ->
                        lot.statusDetails = LotStatusDetails.CANCELLED
                        addLotToLotsResponseDto(lotsResponseDto, lot)
                    }
        }
        tenderProcessDao.save(getEntity(process, entity))
        return ResponseDto(data = CancellationRs(lots = lotsResponseDto))
    }

    fun setCancellation(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
        val token = cm.context.token ?: throw ErrorException(CONTEXT)
        val operationType = cm.context.operationType ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(INVALID_TOKEN)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        validateTenderStatusForCancellation(process, operationType)
        val lotStatusPredicate = getLotStatusPredicateForCancellation(operationType)
        val lotsResponseDto = mutableListOf<LotCancellation>()
        process.tender.apply {
            status = TenderStatus.CANCELLED
            statusDetails = TenderStatusDetails.EMPTY
            lots.asSequence()
                    .filter(lotStatusPredicate)
                    .forEach { lot ->
                        lot.status = LotStatus.CANCELLED
                        lot.statusDetails = LotStatusDetails.EMPTY
                        addLotToLotsResponseDto(lotsResponseDto, lot)
                    }
        }
        tenderProcessDao.save(getEntity(process, entity))
        return ResponseDto(data = CancellationRs(lots = lotsResponseDto))
    }

    fun setStatusDetails(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val phase = cm.context.phase ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.statusDetails = TenderStatusDetails.creator(phase)
        tenderProcessDao.save(getEntity(process, entity))
        return ResponseDto(data = UpdateTenderStatusRs(process.tender.status.key, process.tender.statusDetails.key))
    }

    fun getTenderOwner(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        return ResponseDto(data = GetTenderOwnerRs(entity.owner))
    }

    fun getDataForAc(cm: CommandMessage): ResponseDto {

        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val dto = toObject(GetDataForAcRq::class.java, cm.data)
        val lotsIdsSet = dto.awards.asSequence().map { it.relatedLots[0] }.toSet()

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val lots = process.tender.lots.asSequence().filter { lotsIdsSet.contains(it.id) }.toList()
        if (lots.asSequence().any { it.status == LotStatus.CANCELLED || it.status == LotStatus.UNSUCCESSFUL }) {
            throw ErrorException(INVALID_LOTS_STATUS)
        }
        val items = process.tender.items.asSequence().filter { lotsIdsSet.contains(it.relatedLot) }.toList()
        val contractedTender = GetDataForAcTender(
                id = generationService.generatePermanentTenderId(),
                classification = process.tender.classification,
                procurementMethod = process.tender.procurementMethod,
                procurementMethodDetails = process.tender.procurementMethodDetails,
                mainProcurementCategory = process.tender.mainProcurementCategory,
                lots = lots,
                items = items)
        return ResponseDto(data = GetDataForAcRs(contractedTender))
    }

    private fun getLotStatusPredicateForPrepareCancellation(operationType: String): (Lot) -> Boolean {
        return when (operationType) {
            "cancelTender", "cancellationStandstillPeriod" -> { lot: Lot ->
                (lot.status == LotStatus.ACTIVE)
                        && (lot.statusDetails == LotStatusDetails.EMPTY || lot.statusDetails == LotStatusDetails.AWARDED)
            }
            "cancelPlan" -> { lot: Lot ->
                (lot.status == LotStatus.PLANNING || lot.status == LotStatus.PLANNED)
                        && (lot.statusDetails == LotStatusDetails.EMPTY)
            }
            else -> {
                throw ErrorException(INVALID_OPERATION_TYPE)
            }
        }
    }

    private fun getLotStatusPredicateForCancellation(operationType: String): (Lot) -> Boolean {
        return when (operationType) {
            "cancelTender", "cancelTenderEv" -> { lot: Lot ->
                (lot.status == LotStatus.ACTIVE)
            }
            "cancelPlan" -> { lot: Lot ->
                (lot.status == LotStatus.PLANNING || lot.status == LotStatus.PLANNED)
                        && (lot.statusDetails == LotStatusDetails.EMPTY)
            }
            else -> {
                throw ErrorException(INVALID_OPERATION_TYPE)
            }
        }
    }

    private fun validateTenderStatusForPrepareCancellation(process: TenderProcess, operationType: String) {
        when (operationType) {
            "cancelTender", "cancellationStandstillPeriod" -> {
                if (process.tender.status != TenderStatus.ACTIVE)
                    throw ErrorException(TENDER_IN_UNSUCCESSFUL_STATUS)
            }
        }
    }

    private fun validateTenderStatusForCancellation(process: TenderProcess, operationType: String) {
        when (operationType) {
            "cancelTender", "cancelTenderEv" -> {
                if (process.tender.status != TenderStatus.ACTIVE)
                    throw ErrorException(TENDER_IN_UNSUCCESSFUL_STATUS)
                if (process.tender.statusDetails != TenderStatusDetails.CANCELLATION)
                    throw ErrorException(TENDER_IN_UNSUCCESSFUL_STATUS)
            }
            "cancelPlan" -> {
                if (process.tender.status != TenderStatus.PLANNING && process.tender.status != TenderStatus.PLANNED)
                    throw ErrorException(TENDER_IN_UNSUCCESSFUL_STATUS)
            }
        }
    }

    private fun addLotToLotsResponseDto(lotsResponseDto: MutableList<LotCancellation>, lot: Lot) {
        lotsResponseDto.add(LotCancellation(
                id = lot.id,
                status = lot.status,
                statusDetails = lot.statusDetails))
    }

    private fun getEntity(process: TenderProcess,
                          entity: TenderProcessEntity): TenderProcessEntity {

        return TenderProcessEntity(
                cpId = entity.cpId,
                token = entity.token,
                stage = entity.stage,
                owner = entity.owner,
                createdDate = localNowUTC().toDate(),
                jsonData = toJson(process)
        )
    }

    fun getTenderState(params: GetTenderStateParams): Result<GetTenderStateResult, Fail> {
        val entity = tenderProcessRepository.getByCpIdAndStage(
            cpid = params.cpid, stage = params.ocid.stage
        ).forwardResult { incident -> return incident }
            ?: return ValidationErrors.TenderNotFoundOnGetTenderState(
                cpid = params.cpid, ocid = params.ocid
            ).asFailure()

        val tenderProcess = entity.jsonData
            .tryToObject(TenderProcess::class.java)
            .doReturn { incident ->
                return Fail.Incident.DatabaseIncident(incident.exception).asFailure()
            }
        return tenderProcess.tender.let { tender ->
            GetTenderStateResult(
                status = tender.status,
                statusDetails = tender.statusDetails
            ).asSuccess()
        }
    }
}
