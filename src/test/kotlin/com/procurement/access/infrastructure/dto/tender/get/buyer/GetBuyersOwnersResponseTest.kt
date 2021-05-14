package com.procurement.access.infrastructure.dto.tender.get.buyer

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.response.GetBuyersOwnersResult
import org.junit.jupiter.api.Test

class GetBuyersOwnersResponseTest : AbstractDTOTestBase<GetBuyersOwnersResult>(GetBuyersOwnersResult::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/get/buyer/response_get_buyer.json")
    }
}
