package com.procurement.access.infrastructure.dto.tender.check.currency

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.CheckEqualityCurrenciesRequest
import org.junit.jupiter.api.Test

class CheckEqualityCurrenciesRequestTest
    : AbstractDTOTestBase<CheckEqualityCurrenciesRequest>(CheckEqualityCurrenciesRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/check/currency/request_check_equality_currencies_full.json")
    }
}
