package com.procurement.access.infrastructure.dto

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.request.CheckResponsesRequest
import org.junit.jupiter.api.Test

class CheckResponsesRequestTest : AbstractDTOTestBase<CheckResponsesRequest>(CheckResponsesRequest::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/criteria/response/request_check_responses_full.json")
    }

    @Test
    fun required_1() {
        testBindingAndMapping("json/dto/criteria/response/request_check_responses_required_1.json")
    }

    @Test
    fun required_2() {
        testBindingAndMapping("json/dto/criteria/response/request_check_responses_required_2.json")
    }

    @Test
    fun required_3() {
        testBindingAndMapping("json/dto/criteria/response/request_check_responses_required_3.json")
    }
}
