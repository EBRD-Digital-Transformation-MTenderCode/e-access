package com.procurement.access.infrastructure.dto.rfq.validate

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.ValidateRfqDataRequest
import org.junit.jupiter.api.Test

class ValidateRfqDataRequestTest : AbstractDTOTestBase<ValidateRfqDataRequest>(ValidateRfqDataRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/rfq/validate/request_validate_rfq_full.json")
    }

    @Test
    fun required() {
        testBindingAndMapping("json/dto/rfq/validate/request_validate_rfq_required.json")
    }
}
