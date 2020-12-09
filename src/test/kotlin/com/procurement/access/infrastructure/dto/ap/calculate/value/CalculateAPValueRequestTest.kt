package com.procurement.access.infrastructure.dto.ap.calculate.value

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.CalculateAPValueRequest
import org.junit.jupiter.api.Test

class CalculateAPValueRequestTest : AbstractDTOTestBase<CalculateAPValueRequest>(CalculateAPValueRequest::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/ap/calculate/value/request/calculate_ap_value_request_full.json")
    }
}
