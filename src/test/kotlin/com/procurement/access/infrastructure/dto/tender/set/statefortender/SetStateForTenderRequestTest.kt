package com.procurement.access.infrastructure.dto.tender.set.statefortender

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.set.statefortender.SetStateForTenderRequest
import org.junit.jupiter.api.Test

class SetStateForTenderRequestTest : AbstractDTOTestBase<SetStateForTenderRequest>(SetStateForTenderRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/set/setstatefortender/request_set_state_for_tender_full.json")
    }
}
