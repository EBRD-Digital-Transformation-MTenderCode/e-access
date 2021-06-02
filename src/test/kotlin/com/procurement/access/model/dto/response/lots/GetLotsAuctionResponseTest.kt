package com.procurement.access.model.dto.pn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.GetLotsAuctionResponse
import org.junit.jupiter.api.Test

class GetLotsAuctionResponseTest : AbstractDTOTestBase<GetLotsAuctionResponse>(GetLotsAuctionResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/lot/auction/response/response_get_lots_auction_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/lot/auction/response/response_get_lots_auction_required_1.json")
    }

}