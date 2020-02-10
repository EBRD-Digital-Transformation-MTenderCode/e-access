package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class GetLotIdsResponseTest : AbstractDTOTestBase<GetLotIdsResponse>(GetLotIdsResponse::class.java) {
    @Test
    fun test() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/response/response_get_lot_ids_full.json")
    }
}
