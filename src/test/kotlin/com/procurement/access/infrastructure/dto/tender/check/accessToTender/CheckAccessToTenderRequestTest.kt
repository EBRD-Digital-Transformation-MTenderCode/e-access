package com.procurement.access.infrastructure.dto.tender.check.accessToTender

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.check.accesstotender.CheckAccessToTenderRequest
import org.junit.jupiter.api.Test

class CheckAccessToTenderRequestTest
    : AbstractDTOTestBase<CheckAccessToTenderRequest>(CheckAccessToTenderRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/check/accessToTender/request_check_access_to_tender_full.json")
    }
}
