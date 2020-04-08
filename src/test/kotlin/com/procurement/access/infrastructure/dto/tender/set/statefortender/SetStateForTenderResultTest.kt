package com.procurement.access.infrastructure.dto.tender.set.statefortender

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.set.statefortender.SetStateForTenderResult
import org.junit.jupiter.api.Test

class SetStateForTenderResultTest : AbstractDTOTestBase<SetStateForTenderResult>(SetStateForTenderResult::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/set/setstatefortender/response_set_state_for_tender_full.json")
    }
}
