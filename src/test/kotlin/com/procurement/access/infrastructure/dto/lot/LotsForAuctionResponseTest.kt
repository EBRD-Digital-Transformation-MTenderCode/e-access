package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class LotsForAuctionResponseTest : AbstractDTOTestBase<LotsForAuctionResponse>(LotsForAuctionResponse::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/lot/response/response_lots_for_auction_full.json")
    }
}
