package com.procurement.access.infrastructure.dto.tender.get.buyer

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.GetBuyersOwnersRequest
import org.junit.jupiter.api.Test

class GetBuyersOwnersRequestTest : AbstractDTOTestBase<GetBuyersOwnersRequest>(GetBuyersOwnersRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/get/buyer/request_get_buyer.json")
    }
}
