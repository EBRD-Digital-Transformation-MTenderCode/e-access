package com.procurement.access.infrastructure.dto.tender.get.currency

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.get.currency.GetCurrencyResult
import org.junit.jupiter.api.Test

class GetCurrencyResponseTest :
    AbstractDTOTestBase<GetCurrencyResult>(GetCurrencyResult::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/get/currency/response_get_currency.json")
    }
}
