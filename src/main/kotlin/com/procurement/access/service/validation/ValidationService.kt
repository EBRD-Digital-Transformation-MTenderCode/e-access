package com.procurement.access.service.validation

import com.procurement.access.application.model.params.CheckEqualityCurrenciesParams
import com.procurement.access.application.model.params.CheckExistenceFAParams
import com.procurement.access.application.model.params.CheckExistenceSignAuctionParams
import com.procurement.access.application.model.params.CheckRelationParams
import com.procurement.access.application.model.params.CheckTenderStateParams
import com.procurement.access.application.model.params.ValidateClassificationParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.tender.strategy.check.CheckAccessToTenderParams
import com.procurement.access.application.service.tender.strategy.check.tenderstate.CheckTenderStateStrategy
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.api.v1.ApiResponseV1
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.commandId
import com.procurement.access.infrastructure.api.v1.ocid
import com.procurement.access.infrastructure.entity.TenderClassificationInfo
import com.procurement.access.infrastructure.entity.TenderCurrencyInfo
import com.procurement.access.infrastructure.entity.TenderProcurementMethodModalitiesInfo
import com.procurement.access.infrastructure.handler.v1.model.request.CheckLotStatusRq
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.validation.CheckBid
import com.procurement.access.service.RulesService
import com.procurement.access.service.validation.strategy.CheckAccessToTenderStrategy
import com.procurement.access.service.validation.strategy.CheckItemsStrategy
import com.procurement.access.service.validation.strategy.CheckLotStrategy
import com.procurement.access.service.validation.strategy.award.CheckAwardStrategy
import com.procurement.access.utils.toObject
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

@Service
class ValidationService(
    private val tenderProcessDao: TenderProcessDao,
    private val tenderProcessRepository: TenderProcessRepository,
    private val rulesService: RulesService
) {

    private val checkItemsStrategy = CheckItemsStrategy(tenderProcessDao)
    private val checkAwardStrategy = CheckAwardStrategy(tenderProcessDao)
    private val checkAccessToTenderStrategy = CheckAccessToTenderStrategy(tenderProcessDao, tenderProcessRepository)
    private val checkLotStrategy = CheckLotStrategy(tenderProcessDao)
    private val checkTenderStateStrategy = CheckTenderStateStrategy(tenderProcessRepository, rulesService)

    fun checkBid(cm: CommandMessage): ApiResponseV1.Success {
        val checkDto = toObject(CheckBid::class.java, cm.data)
        val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT)
        val ocid = cm.ocid
        val entity = tenderProcessDao.getByCpidAndOcid(cpId, ocid) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        checkDto.bid.value?.let {
            if (it.currency != process.tender.value.currency) throw ErrorException(ErrorType.INVALID_CURRENCY)
        }
        val lotsId = process.tender.lots.asSequence().map { it.id }.toSet()
        if (!lotsId.containsAll(checkDto.bid.relatedLots)) throw ErrorException(ErrorType.LOT_NOT_FOUND)
        for (lot in process.tender.lots) {
            if (checkDto.bid.relatedLots.contains(lot.id)) {
                if (!(lot.status == LotStatus.ACTIVE && lot.statusDetails == LotStatusDetails.EMPTY)) throw ErrorException(
                    ErrorType.INVALID_LOT_STATUS
                )
            }
        }
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = "ok")
    }

    fun checkItems(cm: CommandMessage): ApiResponseV1.Success {
        val response = checkItemsStrategy.check(cm)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
    }

    fun checkToken(cm: CommandMessage): ApiResponseV1.Success {
        checkAccessToTenderStrategy.checkAccessToTender(cm)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = "ok")
    }

    fun checkAccessToTender(cm: CommandMessage): ApiResponseV1.Success {
        checkAccessToTenderStrategy.checkAccessToTender(cm)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = "ok")
    }

    fun checkAccessToTender(params: CheckAccessToTenderParams): ValidationResult<Fail> {
        return checkAccessToTenderStrategy.checkAccessToTender(params)
    }

    fun checkLotStatus(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT)
        val ocid = cm.ocid
        val stage = cm.context.stage ?: throw ErrorException(ErrorType.CONTEXT)
        val token = cm.context.token ?: throw ErrorException(ErrorType.CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(ErrorType.CONTEXT)
        val lotId = cm.context.id ?: throw ErrorException(ErrorType.CONTEXT)

        val entity = tenderProcessDao.getByCpidAndOcid(cpId, ocid) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.lots.asSequence()
            .firstOrNull { it.id == lotId && it.status == LotStatus.ACTIVE && it.statusDetails == LotStatusDetails.AWARDED }
            ?: throw ErrorException(ErrorType.NO_AWARDED_LOT)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = "ok")
    }

    fun checkExistenceFA(params: CheckExistenceFAParams): ValidationResult<Fail> {
        val cpid = params.cpid
        val stage = Stage.AP

        tenderProcessRepository
            .getByCpIdAndOcid(params.cpid, params.ocid)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return ValidationResult.error(
                ValidationErrors.TenderNotFoundOnCheckExistenceFA(cpid, stage)
            )

        return ValidationResult.ok()
    }

    enum class StageForCheckingRelation { AP, PN }

    fun checkRelation(params: CheckRelationParams): ValidationResult<Fail> {
        val ocid = params.ocid
        val stage = getStageForCheckRelation(ocid)
            .onFailure { return it.reason.asValidationFailure() }

        val checkRelationStrategy: CheckRelationStrategy = when (params.operationType) {
            OperationType.RELATION_AP -> CheckRelationStrategy.RelationApStrategy
            OperationType.CREATE_RFQ -> CheckRelationStrategy.CreateRfqStrategy

            OperationType.AMEND_FE,
            OperationType.APPLY_CONFIRMATIONS,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.AWARD_CONSIDERATION,
            OperationType.COMPLETE_QUALIFICATION,
            OperationType.CREATE_AWARD,
            OperationType.CREATE_CN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_CN_ON_PN,
            OperationType.CREATE_CONTRACT,
            OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
            OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE,
            OperationType.CREATE_FE,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN,
            OperationType.CREATE_PCR,
            OperationType.CREATE_PIN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_PN,
            OperationType.CREATE_SUBMISSION,
            OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.DIVIDE_LOT,
            OperationType.ISSUING_FRAMEWORK_CONTRACT,
            OperationType.NEXT_STEP_AFTER_BUYERS_CONFIRMATION,
            OperationType.NEXT_STEP_AFTER_INVITED_CANDIDATES_CONFIRMATION,
            OperationType.OUTSOURCING_PN,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.QUALIFICATION_PROTOCOL,
            OperationType.START_SECONDSTAGE,
            OperationType.SUBMISSION_PERIOD_END,
            OperationType.SUBMIT_BID,
            OperationType.TENDER_PERIOD_END,
            OperationType.UPDATE_AP,
            OperationType.UPDATE_AWARD,
            OperationType.UPDATE_CN,
            OperationType.UPDATE_PN,
            OperationType.WITHDRAW_BID,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> return ValidationResult.ok()
        }

        return checkRelationStrategy.check(tenderProcessRepository, params, stage)
    }

    private fun getStageForCheckRelation(ocid: Ocid.SingleStage): Result<StageForCheckingRelation, ValidationErrors.InvalidStageOnCheckRelation> =
        when (val stage = ocid.stage) {
            Stage.AP -> StageForCheckingRelation.AP.asSuccess()
            Stage.PN -> StageForCheckingRelation.PN.asSuccess()

            Stage.AC,
            Stage.EI,
            Stage.EV,
            Stage.FE,
            Stage.FS,
            Stage.NP,
            Stage.PC,
            Stage.RQ,
            Stage.TP -> ValidationErrors.InvalidStageOnCheckRelation(stage).asFailure()
        }

    fun checkLotActive(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT)
        val ocid = cm.ocid
        val lotId = cm.context.id ?: throw ErrorException(ErrorType.CONTEXT)

        val entity = tenderProcessDao.getByCpidAndOcid(cpId, ocid) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.lots.asSequence()
            .firstOrNull { it.id == lotId && it.status == LotStatus.ACTIVE && it.statusDetails == LotStatusDetails.EMPTY }
            ?: throw ErrorException(
                error = ErrorType.NO_ACTIVE_LOTS,
                message = "There is no lot with 'status' == ACTIVE & 'statusDetails' == EMPTY by id ${lotId}"
            )
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = "ok")
    }

    fun checkLotsStatus(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT)
        val ocid = cm.ocid
        val lotDto = toObject(CheckLotStatusRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpidAndOcid(cpId, ocid) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)

        val lot = process.tender.lots.asSequence().firstOrNull { it.id == lotDto.relatedLot }
        if (lot != null) {
            if (lot.status != LotStatus.ACTIVE || lot.statusDetails !== LotStatusDetails.EMPTY) {
                throw ErrorException(ErrorType.INVALID_LOT_STATUS)
            }
        } else {
            throw ErrorException(ErrorType.LOT_NOT_FOUND)
        }
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = "Lot status valid.")
    }

    fun checkLotAwarded(cm: CommandMessage): ApiResponseV1.Success {
        checkLotStrategy.check(cm)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = "ok")
    }

    fun checkAward(cm: CommandMessage): ApiResponseV1.Success {
        val response = checkAwardStrategy.check(cm)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
    }

    fun checkTenderState(params: CheckTenderStateParams): ValidationResult<Fail> =
        checkTenderStateStrategy.execute(params)

    fun checkEqualityCurrencies(params: CheckEqualityCurrenciesParams): ValidationResult<Fail> {
        val record = tenderProcessRepository.getByCpIdAndOcid(params.cpid, params.ocid)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return ValidationResult.error(
                ValidationErrors.TenderNotFoundOnCheckEqualityCurrencies(params.cpid, params.ocid)
            )

        val relatedRecord = tenderProcessRepository.getByCpIdAndOcid(params.relatedCpid, params.relatedOcid)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return ValidationResult.error(
                ValidationErrors.RelatedTenderNotFoundOnCheckEqualityCurrencies(params.relatedCpid, params.relatedOcid)
            )

        val tenderCurrency = record.jsonData
            .tryToObject(TenderCurrencyInfo::class.java)
            .onFailure { return it.reason.asValidationFailure() }
        val relatedTenderCurrency = relatedRecord.jsonData
            .tryToObject(TenderCurrencyInfo::class.java)
            .onFailure { return it.reason.asValidationFailure() }

        val currency = tenderCurrency.tender.value.currency
        val relatedCurrency = relatedTenderCurrency.tender.value.currency

        if (currency.toUpperCase() != relatedCurrency.toUpperCase())
            return ValidationResult.error(ValidationErrors.CurrencyDoesNotMatchOnCheckEqualPNAndAPCurrency())

        return ValidationResult.ok()
    }

    fun checkExistenceSignAuction(params: CheckExistenceSignAuctionParams): ValidationResult<Fail> {
        val record = tenderProcessRepository.getByCpIdAndOcid(params.cpid, params.ocid)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return ValidationResult.error(
                ValidationErrors.TenderNotFoundOnCheckExistenceSignAuction(params.cpid, params.ocid)
            )
        val tenderInfo = record.jsonData
            .tryToObject(TenderProcurementMethodModalitiesInfo::class.java)
            .onFailure { return it.reason.asValidationFailure() }

        if (params.containsElectronicAuction() && !tenderInfo.containsElectronicAuction())
            return ValidationResult.error(ValidationErrors.ElectronicAuctionReceivedButNotStored())

        if (!params.containsElectronicAuction() && tenderInfo.containsElectronicAuction())
            return ValidationResult.error(ValidationErrors.ElectronicAuctionNotReceivedButStored())

        return ValidationResult.ok()
    }

    private fun TenderProcurementMethodModalitiesInfo.containsElectronicAuction() =
        tender.procurementMethodModalities?.contains(ProcurementMethodModalities.ELECTRONIC_AUCTION) ?: false

    private fun CheckExistenceSignAuctionParams.containsElectronicAuction() =
        tender?.procurementMethodModalities?.contains(ProcurementMethodModalities.ELECTRONIC_AUCTION) ?: false

    fun validateClassification(params: ValidateClassificationParams): ValidationResult<Fail> {
        val record = tenderProcessRepository.getByCpIdAndOcid(params.cpid, params.ocid)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return ValidationResult.error(
                ValidationErrors.TenderNotFoundOnValidateClassification(params.cpid, params.ocid)
            )
        val tenderInfo = record.jsonData
            .tryToObject(TenderClassificationInfo::class.java)
            .onFailure { return it.reason.asValidationFailure() }

        val receivedClassificationId = params.tender.classification.id
        val storedClassificationId = tenderInfo.tender.classification.id

        if (!storedClassificationId.startsWith(receivedClassificationId.substring(0..2)))
            return ValidationResult.error(
                ValidationErrors.InvalidClassificationId(
                    receivedClassificationId = receivedClassificationId, storedClassidicationId = storedClassificationId
                )
            )

        return ValidationResult.ok()
    }

}
