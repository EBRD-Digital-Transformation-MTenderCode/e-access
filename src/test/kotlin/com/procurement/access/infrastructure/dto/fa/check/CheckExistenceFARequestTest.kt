package com.procurement.access.infrastructure.dto.fa.check

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.check.fa.CheckExistenceFARequest
import org.junit.jupiter.api.Test

class CheckExistenceFARequestTest
    : AbstractDTOTestBase<CheckExistenceFARequest>(CheckExistenceFARequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/fa/check/check_existence_fa_request_full.json")
    }
}
