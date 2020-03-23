package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.get.lotStateByIds.GetLotStateByIdsRequest
import org.junit.jupiter.api.Test

class GetLotStateByIdsRequestTest : AbstractDTOTestBase<GetLotStateByIdsRequest>(GetLotStateByIdsRequest::class.java) {

    @Test
    fun test(){
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/request/request_get_lot_state_by_ids_full.json")
    }

    @Test
    fun test1(){
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/request/request_get_lot_state_by_ids_1.json")
    }
}