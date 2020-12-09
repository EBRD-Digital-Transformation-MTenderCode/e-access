package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.SetStateForLotsRequest
import org.junit.jupiter.api.Test

class SetStateForLotsRequestTest : AbstractDTOTestBase<SetStateForLotsRequest>(
    SetStateForLotsRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/request/request_set_state_for_lots_full.json")
    }
}
