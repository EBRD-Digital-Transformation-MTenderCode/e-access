package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.GetLotResponse
import org.junit.jupiter.api.Test

class GetLotResponseTest : AbstractDTOTestBase<GetLotResponse>(GetLotResponse::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/lot/response/response_get_lot_full.json")
    }
}
