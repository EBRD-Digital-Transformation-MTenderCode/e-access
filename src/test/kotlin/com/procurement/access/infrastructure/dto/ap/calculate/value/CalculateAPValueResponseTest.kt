package com.procurement.access.infrastructure.dto.ap.calculate.value

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.response.CalculateAPValueResult
import org.junit.jupiter.api.Test

class CalculateAPValueResponseTest : AbstractDTOTestBase<CalculateAPValueResult>(CalculateAPValueResult::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/ap/calculate/value/response/calculate_ap_value_response_full.json")
    }
}
