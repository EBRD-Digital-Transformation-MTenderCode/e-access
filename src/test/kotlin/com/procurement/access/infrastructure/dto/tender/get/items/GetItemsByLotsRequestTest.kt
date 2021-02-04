package com.procurement.access.infrastructure.dto.tender.get.items

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.request.GetItemsByLotsRequest
import org.junit.jupiter.api.Test

class GetItemsByLotsRequestTest : AbstractDTOTestBase<GetItemsByLotsRequest>(GetItemsByLotsRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/get/items/request_get_items_by_lots_full.json")
    }
}
