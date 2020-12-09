package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.CheckLotsStateRequest
import org.junit.jupiter.api.Test

class CheckLotsStateRequestTest : AbstractDTOTestBase<CheckLotsStateRequest>(
    CheckLotsStateRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/request/request_check_lots_state_full.json")
    }
}
