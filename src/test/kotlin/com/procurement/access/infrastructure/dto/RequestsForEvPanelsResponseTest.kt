package com.procurement.access.infrastructure.dto

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.RequestsForEvPanelsResponse
import org.junit.jupiter.api.Test

class RequestsForEvPanelsResponseTest :
    AbstractDTOTestBase<RequestsForEvPanelsResponse>(RequestsForEvPanelsResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/criteria/response_requests_for_ev_panels_criteria_full.json")
    }
}
