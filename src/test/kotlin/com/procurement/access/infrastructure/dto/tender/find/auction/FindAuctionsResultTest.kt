package com.procurement.access.infrastructure.dto.tender.find.auction

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.find.auction.FindAuctionsResult
import org.junit.jupiter.api.Test

class FindAuctionsResultTest :
    AbstractDTOTestBase<FindAuctionsResult>(FindAuctionsResult::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/find/auction/result_find_auctions_full.json")
    }
}
