package com.procurement.access.model.dto.pn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.model.dto.response.GetLotsAuctionResponse
import org.junit.jupiter.api.Test

class GetLotsAuctionResponseTest : AbstractDTOTestBase<GetLotsAuctionResponse>(GetLotsAuctionResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/lot/auction/response/response_get_lots_auction_full.json")
    }

}