package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.GetActiveLotsResponse
import org.junit.jupiter.api.Test

class GetActiveLotsResponseTest : AbstractDTOTestBase<GetActiveLotsResponse>(GetActiveLotsResponse::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/lots/response_get_active_lots_full.json")
    }
}
