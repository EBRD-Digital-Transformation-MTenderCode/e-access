package com.procurement.access.infrastructure.dto.tender.prepare.cancellation

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.PrepareCancellationResponse
import org.junit.jupiter.api.Test

class PrepareCancellationResponseTest :
    AbstractDTOTestBase<PrepareCancellationResponse>(PrepareCancellationResponse::class.java) {

    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/prepare/cancellation/response/response_prepare_cancellation_fully.json")
    }
}
