package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.get.lotids.FindLotIdsRequest
import org.junit.jupiter.api.Test

class FindLotIdsRequestTest : AbstractDTOTestBase<FindLotIdsRequest>(
    FindLotIdsRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/request/request_find_lot_ids_full.json")
    }

    @Test
    fun test1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/request/request_find_lot_ids_1.json")
    }
}
