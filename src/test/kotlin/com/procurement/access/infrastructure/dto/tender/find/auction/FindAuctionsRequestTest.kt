package com.procurement.access.infrastructure.dto.tender.find.auction

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.FindAuctionsRequest
import org.junit.jupiter.api.Test

class FindAuctionsRequestTest :
    AbstractDTOTestBase<FindAuctionsRequest>(FindAuctionsRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/find/auction/request_find_auctions_full.json")
    }
}
