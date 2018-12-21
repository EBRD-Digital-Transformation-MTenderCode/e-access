package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.CommandMessage
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.lots.CheckLotStatusDetailsRs
import com.procurement.access.model.dto.lots.CheckLotStatusRq
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.Operation.*
import com.procurement.access.model.dto.validation.*
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

@Service
class ValidationService(private val tenderProcessDao: TenderProcessDao) {

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
                if (!(lot.status == LotStatus.ACTIVE && lot.statusDetails == LotStatusDetails.EMPTY)) throw ErrorException(ErrorType.INVALID_LOT_STATUS)
            }
        }
        return ResponseDto(data = "ok")
    }

    fun checkItems(cm: CommandMessage): ResponseDto {
        val checkDto = toObject(CheckItemsRq::class.java, cm.data)
        val operationType = cm.context.operationType ?: throw ErrorException(ErrorType.CONTEXT)
        val operation = Operation.fromValue(operationType)
        if ((operation == CREATE_CN_ON_PN) || (operation == CREATE_PIN_ON_PN)) {
            val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT)
            val stage = cm.context.prevStage ?: throw ErrorException(ErrorType.CONTEXT)
            val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage)
                    ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
            val process = toObject(TenderProcess::class.java, entity.jsonData)
            validateTenderStatus(process)
            return if (process.tender.items.isEmpty()) {
                checkItemCodes(checkDto.items, 3)
                val classificationClass = calculateClassificationClass(checkDto)
                checkClassificationClass(process.tender.classification.id, classificationClass, 3)
                ResponseDto(data = CheckItemsRs(
                        mdmValidation = true,
                        itemsAdd = true,
                        tender = TenderCheck(classification = ClassificationCheck(id = classificationClass))))
            } else {
                checkItemsSizeAndIds(process.tender.items, checkDto.items)
                ResponseDto(data = CheckItemsRs(
                        mdmValidation = false,
                        itemsAdd = false)
                )
            }
        } else if ((operation == CREATE_CN) || (operation == CREATE_PN) || (operation == CREATE_PIN)) {
            checkItemCodes(checkDto.items, 3)
            val classificationClass = calculateClassificationClass(checkDto)
            return ResponseDto(data = CheckItemsRs(
                    mdmValidation = true,
                    itemsAdd = true,
                    tender = TenderCheck(classification = ClassificationCheck(id = classificationClass))))
        } else if (operation == UPDATE_PN) {
            val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT)
            val stage = cm.context.stage ?: throw ErrorException(ErrorType.CONTEXT)
            val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage)
                    ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
            val process = toObject(TenderProcess::class.java, entity.jsonData)
            validateTenderStatus(process)
            return if (process.tender.items.isEmpty()) {
                checkItemCodes(checkDto.items, 3)
                val classificationClass = calculateClassificationClass(checkDto)
                checkClassificationClass(process.tender.classification.id, classificationClass, 3)
                ResponseDto(data = CheckItemsRs(
                        mdmValidation = true,
                        itemsAdd = true,
                        tender = TenderCheck(classification = ClassificationCheck(id = classificationClass))))
            } else {
                checkItemsSizeAndIds(process.tender.items, checkDto.items)
                ResponseDto(data = CheckItemsRs(
                        mdmValidation = true,
                        itemsAdd = false)
                )
            }
        } else
            return getNegativeResponse()
    }

    fun checkToken(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(ErrorType.CONTEXT)
        val token = cm.context.token ?: throw ErrorException(ErrorType.CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(ErrorType.CONTEXT)
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        return ResponseDto(data = "ok")
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

    private fun checkItemsSizeAndIds(items: List<Item>, itemsDto: List<ItemCheck>) {
        if (items.size != itemsDto.size) throw ErrorException(ErrorType.INVALID_ITEMS)
        val itemsIds = items.map { it.id }.toSet()
        val itemsDtoIds = itemsDto.map { it.id }.toSet()
        if (!itemsIds.containsAll(itemsDtoIds)) throw ErrorException(ErrorType.INVALID_ITEMS)
    }

    private fun validateTenderStatus(process: TenderProcess) {
        if (process.tender.status == TenderStatus.UNSUCCESSFUL) throw ErrorException(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS)
    }

    private fun calculateClassificationClass(checkDto: CheckItemsRq): String {
        val commonChars = getCommonChars(checkDto.items, 3, 7)
        return commonChars.padEnd(8, '0')
    }

    private fun getNegativeResponse(): ResponseDto {
        return ResponseDto(data = CheckItemsRs(mdmValidation = false, tender = null))
    }

    private fun checkClassificationClass(classificationIdDB: String, commonClass: String, charCount: Int) {
        if (classificationIdDB.take(charCount) != commonClass.take(charCount))
            throw ErrorException(ErrorType.INVALID_ITEMS)
    }

    private fun checkItemCodes(items: List<ItemCheck>, charCount: Int) {
        val itemsId = items.asSequence().map { it.id }.toHashSet()
        if (itemsId.size != items.size) throw ErrorException(ErrorType.INVALID_ITEMS)
        if (items.asSequence().map { it.classification.id.take(charCount) }.toSet().size > 1)
            throw ErrorException(ErrorType.INVALID_ITEMS)
    }

    private fun getCommonChars(items: List<ItemCheck>, countFrom: Int, countTo: Int): String {
        var commonChars = ""
        for (count in countFrom..countTo) {
            val itemClass = items.asSequence().map { it.classification.id.take(count) }.toSet()
            if (itemClass.size > 1) {
                return commonChars
            } else {
                commonChars = itemClass.first()
            }
        }
        return commonChars
    }

}
