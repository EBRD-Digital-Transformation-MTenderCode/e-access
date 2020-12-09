package com.procurement.access.infrastructure.dto.criteria

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.GetCriteriaResponse
import org.junit.jupiter.api.Test

class GetCriteriaResponseTest : AbstractDTOTestBase<GetCriteriaResponse>(GetCriteriaResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/criteria/get/response_get_criteria_full.json")
    }

    @Test
    fun required_1() {
        testBindingAndMapping("json/dto/criteria/get/response_get_criteria_required_1.json")
    }

    @Test
    fun required_2() {
        testBindingAndMapping("json/dto/criteria/get/response_get_criteria_required_2.json")
    }
}
