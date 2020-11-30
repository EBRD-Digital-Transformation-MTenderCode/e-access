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
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.api.v1.ApiResponseV1
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.commandId
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.TenderClassificationInfo
import com.procurement.access.infrastructure.entity.TenderCurrencyInfo
import com.procurement.access.infrastructure.entity.TenderProcurementMethodModalitiesInfo
import com.procurement.access.infrastructure.entity.process.RelatedProcess
import com.procurement.access.infrastructure.handler.v1.model.request.CheckBSRq
import com.procurement.access.infrastructure.handler.v1.model.request.CheckLotStatusRq
import com.procurement.access.lib.extension.toSet
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.validation.CheckBid
import com.procurement.access.service.RulesService
import com.procurement.access.service.validation.strategy.CheckItemsStrategy
import com.procurement.access.service.validation.strategy.CheckLotStrategy
import com.procurement.access.service.validation.strategy.CheckOwnerAndTokenStrategy
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
    private val checkOwnerAndTokenStrategy = CheckOwnerAndTokenStrategy(tenderProcessDao, tenderProcessRepository)
    private val checkLotStrategy = CheckLotStrategy(tenderProcessDao)
    private val checkTenderStateStrategy = CheckTenderStateStrategy(tenderProcessRepository, rulesService)

    fun checkBid(cm: CommandMessage): ApiResponseV1.Success {
        val checkDto = toObject(CheckBid::class.java, cm.data)
        val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(ErrorType.CONTEXT)
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
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
        checkOwnerAndTokenStrategy.checkOwnerAndToken(cm)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = "ok")
    }

    fun checkOwnerAndToken(cm: CommandMessage): ApiResponseV1.Success {
        checkOwnerAndTokenStrategy.checkOwnerAndToken(cm)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = "ok")
    }

    fun checkOwnerAndToken(params: CheckAccessToTenderParams): ValidationResult<Fail> {
        return checkOwnerAndTokenStrategy.checkOwnerAndToken(params)
    }

    fun checkLotStatus(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(ErrorType.CONTEXT)
        val token = cm.context.token ?: throw ErrorException(ErrorType.CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(ErrorType.CONTEXT)
        val lotId = cm.context.id ?: throw ErrorException(ErrorType.CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
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
            .getByCpIdAndStage(cpid, stage)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return ValidationResult.error(
                ValidationErrors.TenderNotFoundOnCheckExistenceFA(cpid, stage)
            )

        return ValidationResult.ok()
    }

    fun checkRelation(params: CheckRelationParams): ValidationResult<Fail> {
        val cpid = params.cpid
        val stage = params.ocid.stage

        val entity = tenderProcessRepository
            .getByCpIdAndStage(cpid, stage)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return ValidationResult.error(
                ValidationErrors.TenderNotFoundOnCheckRelation(cpid, params.ocid)
            )

        return when(params.operationType) {
            OperationType.RELATION_AP -> {
                val relatedProcesses = entity.jsonData.tryToObject(APEntity::class.java)
                    .onFailure { return it.reason.asValidationFailure() }
                    .relatedProcesses

                if (params.existenceRelation)
                    checkRelationExistsOnAp(relatedProcesses, params)
                else
                    checkRelationNotExistsOnAp(relatedProcesses, params)
            }

            OperationType.AMEND_FE,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.COMPLETE_QUALIFICATION,
            OperationType.CREATE_AWARD,
            OperationType.CREATE_CN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_CN_ON_PN,
            OperationType.CREATE_FE,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN,
            OperationType.CREATE_PCR,
            OperationType.CREATE_PIN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_PN,
            OperationType.CREATE_SUBMISSION,
            OperationType.OUTSOURCING_PN,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_PROTOCOL,
            OperationType.START_SECONDSTAGE,
            OperationType.SUBMISSION_PERIOD_END,
            OperationType.TENDER_PERIOD_END,
            OperationType.UPDATE_AP,
            OperationType.UPDATE_CN,
            OperationType.UPDATE_PN,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> ValidationResult.ok()
        }
    }


    val checkRelationExistsOnApPredicate: (RelatedProcess, Cpid) -> Boolean = { relatedProcess, cpid ->
        relatedProcess.relationship.any { it == RelatedProcessType.FRAMEWORK }
            && relatedProcess.identifier == cpid.toString()
    }

    fun checkRelationExistsOnAp(relatedProcesses: List<RelatedProcess>?, params: CheckRelationParams): ValidationResult<ValidationErrors> {

        if (relatedProcesses == null || relatedProcesses.isEmpty())
            return ValidationResult.error(
                ValidationErrors.RelatedProcessNotExistsOnCheckRelation(params.cpid, params.ocid)
            )
        else {
            val isMissing = relatedProcesses.none { checkRelationExistsOnApPredicate(it, params.relatedCpid) }
            if (isMissing)
                return ValidationResult.error(
                    ValidationErrors.MissingAttributesOnCheckRelation(
                        relatedCpid = params.relatedCpid, cpid = params.cpid, ocid = params.ocid
                    )
                )
        }

        return ValidationResult.ok()
    }

    val checkRelationNotExistsOnApPredicate: (RelatedProcess, Cpid) -> Boolean = { relatedProcess, cpid ->
        relatedProcess.relationship.any { it == RelatedProcessType.X_SCOPE }
            && relatedProcess.identifier == cpid.toString()
    }

    fun checkRelationNotExistsOnAp(
        relatedProcesses: List<RelatedProcess>?,
        params: CheckRelationParams
    ): ValidationResult<ValidationErrors> {

        if (relatedProcesses == null || relatedProcesses.isEmpty())
            return ValidationResult.ok()
        else {
            relatedProcesses.forEach { relatedProcess ->
                if (checkRelationNotExistsOnApPredicate(relatedProcess, params.relatedCpid))
                    return ValidationResult.error(
                        ValidationErrors.UnexpectedAttributesValueOnCheckRelation(
                            id = relatedProcess.id,
                            relatedCpid = params.relatedCpid,
                            cpid = params.cpid,
                            ocid = params.ocid
                        )
                    )
            }
        }

        return ValidationResult.ok()
    }

    fun checkLotActive(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(ErrorType.CONTEXT)
        val lotId = cm.context.id ?: throw ErrorException(ErrorType.CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
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
        val stage = cm.context.stage ?: throw ErrorException(ErrorType.CONTEXT)
        val lotDto = toObject(CheckLotStatusRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
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

    fun checkBudgetSources(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT)
        val bsDto = toObject(CheckBSRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, "EV") ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val bbIds = process.planning.budget.budgetBreakdown.toSet { it.id }
        val bsIds = bsDto.planning.budget.budgetSource.toSet { it.budgetBreakdownID }
        if (!bbIds.containsAll(bsIds)) throw ErrorException(ErrorType.INVALID_BS)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = "Budget sources are valid.")
    }

    fun checkAward(cm: CommandMessage): ApiResponseV1.Success {
        val response = checkAwardStrategy.check(cm)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = response)
    }

    fun checkTenderState(params: CheckTenderStateParams): ValidationResult<Fail> =
        checkTenderStateStrategy.execute(params)

    fun checkEqualityCurrencies(params: CheckEqualityCurrenciesParams): ValidationResult<Fail> {
        val record = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return ValidationResult.error(
                ValidationErrors.TenderNotFoundOnCheckEqualityCurrencies(params.cpid, params.ocid)
            )

        val relatedRecord = tenderProcessRepository.getByCpIdAndStage(params.relatedCpid, params.relatedOcid.stage)
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
        val record = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
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
        val record = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
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
