package com.procurement.access.infrastructure.dto.rfq.create

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.response.CreateRfqResult
import org.junit.jupiter.api.Test

class CreateRfqResultTest : AbstractDTOTestBase<CreateRfqResult>(CreateRfqResult::class.java) {

    @Test
    fun full() {
        testBindingAndMapping("json/dto/rfq/create/response_create_rfq_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/rfq/create/response_create_rfq_required_1.json")
    }
}
