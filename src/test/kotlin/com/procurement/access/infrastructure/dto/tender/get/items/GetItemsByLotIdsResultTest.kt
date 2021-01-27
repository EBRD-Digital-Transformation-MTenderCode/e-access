package com.procurement.access.infrastructure.dto.tender.get.items

import com.procurement.access.application.service.tender.strategy.get.items.GetItemsByLotIdsResult
import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class GetItemsByLotIdsResultTest : AbstractDTOTestBase<GetItemsByLotIdsResult>(GetItemsByLotIdsResult::class.java) {

    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/get/items/result_get_items_by_lot_ids_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/tender/get/items/result_get_items_by_lot_ids_required_1.json")
    }

}
