package com.procurement.access.infrastructure.dto.tender.prepare.cancellation

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.request.PrepareCancellationRequest
import org.junit.jupiter.api.Test

class PrepareCancellationRequestTest :
    AbstractDTOTestBase<PrepareCancellationRequest>(PrepareCancellationRequest::class.java) {

    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/prepare/cancellation/request/request_prepare_cancellation_fully.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/tender/prepare/cancellation/request/request_prepare_cancellation_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/tender/prepare/cancellation/request/request_prepare_cancellation_required_2.json")
    }
}
