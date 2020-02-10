package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class GetLotIdsRequestTest : AbstractDTOTestBase<GetLotIdsRequest>(GetLotIdsRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/request/request_get_lot_ids_full.json")
    }

    @Test
    fun test1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/request/request_get_lot_ids_1.json")
    }
}
