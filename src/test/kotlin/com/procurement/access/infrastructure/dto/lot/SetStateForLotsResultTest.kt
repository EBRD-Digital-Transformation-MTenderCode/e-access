package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.response.SetStateForLotsResult
import org.junit.jupiter.api.Test

class SetStateForLotsResultTest : AbstractDTOTestBase<SetStateForLotsResult>(
    SetStateForLotsResult::class.java) {
    @Test
    fun test() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/response/response_set_state_for_lots_full.json")
    }
}
