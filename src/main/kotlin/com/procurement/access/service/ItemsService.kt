package com.procurement.access.service

import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.items.ItemItemsRq
import com.procurement.access.model.dto.items.ItemsRq
import com.procurement.access.model.dto.items.getItemsRs
import org.springframework.stereotype.Service

interface ItemsService {

    fun checkItems(country: String,
                   pmd: String,
                   itemsDto: ItemsRq): ResponseDto
}

@Service
class ItemsServiceImpl : ItemsService {

    override fun checkItems(country: String,
                            pmd: String,
                            itemsDto: ItemsRq): ResponseDto {
        checkItemCodes(itemsDto.items, 3)
        val commonChars = getCommonChars(itemsDto.items, 3, 7)
        val commonClass = addCheckSum(commonChars)
        return ResponseDto(true, null, getItemsRs(commonClass))
    }


    private fun checkItemCodes(items: HashSet<ItemItemsRq>, charCount: Int) {
        if (items.asSequence().map { it.classification.id.take(charCount) }.toSet().size > 1) throw ErrorException(ErrorType.INVALID_ITEMS)
    }

    private fun getCommonChars(items: HashSet<ItemItemsRq>, countFrom: Int, countTo: Int): String {
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

    private fun addCheckSum(commonChars: String): String {
        var classOfItems = commonChars
        val length = commonChars.length
        for (c in length..7) classOfItems = classOfItems.plus("0")
        val n1 = classOfItems[0].toString().toInt()
        val n2 = classOfItems[1].toString().toInt()
        val n3 = classOfItems[2].toString().toInt()
        val n4 = classOfItems[3].toString().toInt()
        val n5 = classOfItems[4].toString().toInt()
        val n6 = classOfItems[5].toString().toInt()
        val n7 = classOfItems[6].toString().toInt()
        val n8 = classOfItems[7].toString().toInt()
        val checkSum: Int = (n1 * 3 + n2 * 7 + n3 * 1 + n4 * 3 + n5 * 7 + n6 * 1 + n7 * 3 + n8 * 7) / 10
        return "$classOfItems-$checkSum"
    }
}