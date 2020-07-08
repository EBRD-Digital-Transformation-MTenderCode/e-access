package com.procurement.access.infrastructure.dto.tender.check.tenderstate

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.check.tenderstate.CheckTenderStateRequest
import org.junit.jupiter.api.Test

class CheckTenderStateRequestTest
    : AbstractDTOTestBase<CheckTenderStateRequest>(CheckTenderStateRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/check/tenderstate/check_tender_state_request_full.json")
    }
}
