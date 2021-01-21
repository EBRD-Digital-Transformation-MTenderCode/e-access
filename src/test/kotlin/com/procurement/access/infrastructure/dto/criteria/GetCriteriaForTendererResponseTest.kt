package com.procurement.access.infrastructure.dto.criteria

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.GetCriteriaForTendererResponse
import org.junit.jupiter.api.Test

class GetCriteriaForTendererResponseTest :
    AbstractDTOTestBase<GetCriteriaForTendererResponse>(GetCriteriaForTendererResponse::class.java) {

    @Test
    fun test() {
        testBindingAndMapping("json/dto/criteria/get/response_get_criteria_for_tenderer_full.json")
    }
}
