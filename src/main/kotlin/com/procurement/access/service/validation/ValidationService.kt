package com.procurement.access.service.validation

import com.procurement.access.application.model.params.CheckTenderStateParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.tender.strategy.check.CheckAccessToTenderParams
import com.procurement.access.application.service.tender.strategy.check.tenderstate.CheckTenderStateStrategy
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.ResponseDto
import com.procurement.access.model.dto.lots.CheckLotStatusRq
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.validation.CheckBSRq
import com.procurement.access.model.dto.validation.CheckBid
import com.procurement.access.service.RulesService
import com.procurement.access.service.validation.strategy.CheckItemsStrategy
import com.procurement.access.service.validation.strategy.CheckLotStrategy
import com.procurement.access.service.validation.strategy.CheckOwnerAndTokenStrategy
import com.procurement.access.service.validation.strategy.award.CheckAwardStrategy
import com.procurement.access.utils.toObject
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

    fun checkBid(cm: CommandMessage): ResponseDto {
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
        return ResponseDto(data = "ok")
    }

    fun checkItems(cm: CommandMessage): ResponseDto {
        val response = checkItemsStrategy.check(cm)
        return ResponseDto(data = response)
    }

    fun checkToken(cm: CommandMessage): ResponseDto {
        checkOwnerAndTokenStrategy.checkOwnerAndToken(cm)
        return ResponseDto(data = "ok")
    }

    fun checkOwnerAndToken(cm: CommandMessage): ResponseDto {
        checkOwnerAndTokenStrategy.checkOwnerAndToken(cm)
        return ResponseDto(data = "ok")
    }

    fun checkOwnerAndToken(params: CheckAccessToTenderParams): ValidationResult<Fail> {
        return checkOwnerAndTokenStrategy.checkOwnerAndToken(params)
    }

    fun checkLotStatus(cm: CommandMessage): ResponseDto {
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
        return ResponseDto(data = "ok")
    }

    fun checkLotActive(cm: CommandMessage): ResponseDto {
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
        return ResponseDto(data = "ok")
    }

    fun checkLotsStatus(cm: CommandMessage): ResponseDto {
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
        return ResponseDto(data = "Lot status valid.")
    }

    fun checkLotAwarded(cm: CommandMessage): ResponseDto {
        checkLotStrategy.check(cm)
        return ResponseDto(data = "ok")
    }

    fun checkBudgetSources(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT)
        val bsDto = toObject(CheckBSRq::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, "EV") ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val bbIds = process.planning.budget.budgetBreakdown.asSequence().map { it.id }.toHashSet()
        val bsIds = bsDto.planning.budget.budgetSource.asSequence().map { it.budgetBreakdownID }.toHashSet()
        if (!bbIds.containsAll(bsIds)) throw ErrorException(ErrorType.INVALID_BS)
        return ResponseDto(data = "Budget sources are valid.")
    }

    fun checkAward(cm: CommandMessage): ResponseDto {
        val response = checkAwardStrategy.check(cm)
        return ResponseDto(id = cm.id, data = response)
    }

    fun checkTenderState(params: CheckTenderStateParams): ValidationResult<Fail> =
        checkTenderStateStrategy.execute(params)
}
