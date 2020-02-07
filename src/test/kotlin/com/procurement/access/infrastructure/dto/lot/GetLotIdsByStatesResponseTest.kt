package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class GetLotIdsByStatesResponseTest : AbstractDTOTestBase<GetLotIdsByStatesResponse>(GetLotIdsByStatesResponse::class.java) {
    @Test
    fun test() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/response/response_get_lot_ids_by_states_full.json")
    }
}
