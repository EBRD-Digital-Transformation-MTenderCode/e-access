package com.procurement.access.infrastructure.dto.tender.check.auction

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.CheckExistenceSignAuctionRequest
import org.junit.jupiter.api.Test

class CheckExistenceSignAuctionRequestTest
    : AbstractDTOTestBase<CheckExistenceSignAuctionRequest>(CheckExistenceSignAuctionRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/check/auction/request_check_existence_sign_auction_full.json")
    }

    @Test
    fun required() {
        testBindingAndMapping("json/dto/tender/check/auction/request_check_existence_sign_auction_required.json")
    }
}
