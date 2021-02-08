package com.procurement.access.infrastructure.dto.tender.get.items

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.GetItemsByLotsResponse
import org.junit.jupiter.api.Test

class GetItemsByLotsResponseTest : AbstractDTOTestBase<GetItemsByLotsResponse>(GetItemsByLotsResponse::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/get/items/response_get_items_by_lots_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/tender/get/items/response_get_items_by_lots_required_1.json")
    }
}
