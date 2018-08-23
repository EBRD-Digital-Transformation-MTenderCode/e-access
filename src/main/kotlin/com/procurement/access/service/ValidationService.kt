package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.CommandMessage
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.ocds.Operation
import com.procurement.access.model.dto.ocds.Operation.*
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import com.procurement.access.model.dto.validation.*
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

interface ValidationService {

    fun checkBid(cm: CommandMessage): ResponseDto

    fun checkItems(cm: CommandMessage): ResponseDto

    fun checkToken(cm: CommandMessage): ResponseDto
}

@Service
class ValidationServiceImpl(private val tenderProcessDao: TenderProcessDao) : ValidationService {

    override fun checkBid(cm: CommandMessage): ResponseDto {
        val checkDto = toObject(CheckBid::class.java, cm.data)
        val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT_PARAM_NOT_FOUND)
        val stage = cm.context.stage ?: throw ErrorException(ErrorType.CONTEXT_PARAM_NOT_FOUND)
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        checkDto.bid.value?.let {
            if (it.currency != process.tender.value.currency) throw ErrorException(ErrorType.INVALID_CURRENCY)
        }
        val lotsId = process.tender.lots.asSequence().map { it.id }.toSet()
        if (!lotsId.containsAll(checkDto.bid.relatedLots)) throw ErrorException(ErrorType.LOT_NOT_FOUND)
        for (lot in process.tender.lots) {
            if (checkDto.bid.relatedLots.contains(lot.id)) {
                if (!(lot.status == TenderStatus.ACTIVE && lot.statusDetails == TenderStatusDetails.EMPTY)) throw ErrorException(ErrorType.INVALID_LOT_STATUS)
                checkDto.bid.value?.let {
                    if (it.amount > lot.value.amount) throw ErrorException(ErrorType.BID_VALUE_MORE_THAN_SUM_LOT)
                }
            }
        }
        return ResponseDto(data = "ok")
    }

    override fun checkItems(cm: CommandMessage): ResponseDto {
        val checkDto = toObject(CheckItemsRq::class.java, cm.data)
        val operationType = cm.context.operationType ?: throw ErrorException(ErrorType.CONTEXT_PARAM_NOT_FOUND)
        val commonChars = getCommonChars(checkDto.items, 3, 7)
        val commonClass = commonChars.padEnd(8, '0')
        val operation = Operation.fromValue(operationType)
        if ((operation == CREATE_CN) || (operation == CREATE_PN) || (operation == CREATE_PIN)) {
            return validateItemsAndGetResponse(checkDto, commonClass)
        } else if ((operation == UPDATE_CN) || (operation == UPDATE_PN)) {
            val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT_PARAM_NOT_FOUND)
            val stage = cm.context.stage ?: throw ErrorException(ErrorType.CONTEXT_PARAM_NOT_FOUND)
            val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage)
                    ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
            val process = toObject(TenderProcess::class.java, entity.jsonData)
            validateTenderStatus(process)
            return validateItemsAndCommonClassAndGetResponse(checkDto, process, commonClass)
        } else if ((operation == CREATE_CN_ON_PN) || (operation == CREATE_PIN_ON_PN)) {
            val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT_PARAM_NOT_FOUND)
            val stage = cm.context.stage ?: throw ErrorException(ErrorType.CONTEXT_PARAM_NOT_FOUND)
            val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage)
                    ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
            val process = toObject(TenderProcess::class.java, entity.jsonData)
            validateTenderStatus(process)
            return if (process.tender.items.isEmpty()) {
                validateItemsAndCommonClassAndGetResponse(checkDto, process, commonClass)
            } else {
                getNegativeResponse()
            }
        }
        return getNegativeResponse()
    }

    private fun validateTenderStatus(process: TenderProcess) {
        if(process.tender.status == TenderStatus.UNSUCCESSFUL) throw ErrorException(ErrorType.INVALID_TENDER_STATUS)
    }

    override fun checkToken(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(ErrorType.CONTEXT_PARAM_NOT_FOUND)
        val stage = cm.context.stage ?: throw ErrorException(ErrorType.CONTEXT_PARAM_NOT_FOUND)
        val token = cm.context.token ?: throw ErrorException(ErrorType.CONTEXT_PARAM_NOT_FOUND)
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        return ResponseDto(data = "ok")
    }

    private fun validateItemsAndGetResponse(checkDto: CheckItemsRq, commonClass: String): ResponseDto {
        checkItemCodes(checkDto.items, 3)
        return ResponseDto(data = CheckItemsRs(mdmValidation = true, tender = TenderCheck(classification = ClassificationCheck(id = commonClass))))
    }

    private fun validateItemsAndCommonClassAndGetResponse(checkDto: CheckItemsRq, process: TenderProcess, commonClass: String): ResponseDto {
        checkItemCodes(checkDto.items, 3)
        checkCommonClass(process.tender.classification.id, commonClass, 3)
        return ResponseDto(data = CheckItemsRs(mdmValidation = true, tender = TenderCheck(classification = ClassificationCheck(id = commonClass))))
    }

    private fun getNegativeResponse(): ResponseDto {
        return ResponseDto(data = CheckItemsRs(mdmValidation = false, tender = null))
    }

    private fun checkCommonClass(classificationIdDB: String, commonClass: String, charCount: Int) {
        if (classificationIdDB.take(charCount) != commonClass.take(charCount))
            throw ErrorException(ErrorType.INVALID_ITEMS)
    }

    private fun checkItemCodes(items: HashSet<ItemCheck>, charCount: Int) {
        if (items.asSequence().map { it.classification.id.take(charCount) }.toSet().size > 1)
            throw ErrorException(ErrorType.INVALID_ITEMS)
    }

    private fun getCommonChars(items: HashSet<ItemCheck>, countFrom: Int, countTo: Int): String {
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
