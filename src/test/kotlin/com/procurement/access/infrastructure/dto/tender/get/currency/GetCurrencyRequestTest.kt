package com.procurement.access.infrastructure.dto.tender.get.currency

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.get.currency.GetCurrencyRequest
import org.junit.jupiter.api.Test

class GetCurrencyRequestTest :
    AbstractDTOTestBase<GetCurrencyRequest>(GetCurrencyRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/get/currency/request_get_currency.json")
    }
}
