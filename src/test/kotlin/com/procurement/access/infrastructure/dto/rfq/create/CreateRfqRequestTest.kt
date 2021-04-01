package com.procurement.access.infrastructure.dto.rfq.create

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.CreateRfqRequest
import org.junit.jupiter.api.Test

class CreateRfqRequestTest : AbstractDTOTestBase<CreateRfqRequest>(CreateRfqRequest::class.java) {

    @Test
    fun full() {
        testBindingAndMapping("json/dto/rfq/create/request_create_rfq_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/rfq/create/request_create_rfq_required_1.json")
    }
}
