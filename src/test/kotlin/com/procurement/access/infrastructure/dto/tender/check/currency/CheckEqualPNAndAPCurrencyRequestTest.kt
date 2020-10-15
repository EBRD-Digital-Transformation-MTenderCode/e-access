package com.procurement.access.infrastructure.dto.tender.check.currency

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.check.currency.CheckEqualPNAndAPCurrencyRequest
import org.junit.jupiter.api.Test

class CheckEqualPNAndAPCurrencyRequestTest
    : AbstractDTOTestBase<CheckEqualPNAndAPCurrencyRequest>(CheckEqualPNAndAPCurrencyRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/check/currency/request_check_equal_pn_and_ap_currency_full.json")
    }
}
