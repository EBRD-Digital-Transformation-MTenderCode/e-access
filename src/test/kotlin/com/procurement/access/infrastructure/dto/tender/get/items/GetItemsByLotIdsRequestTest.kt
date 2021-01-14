package com.procurement.access.infrastructure.dto.tender.get.items

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.GetItemsByLotIdsRequest
import org.junit.jupiter.api.Test

class GetItemsByLotIdsRequestTest : AbstractDTOTestBase<GetItemsByLotIdsRequest>(GetItemsByLotIdsRequest::class.java) {

    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/get/items/request_get_items_by_lot_ids_full.json")
    }

}
