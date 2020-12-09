package com.procurement.access.infrastructure.dto.tender.get.awardCriteria

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.GetAwardCriteriaResponse
import org.junit.jupiter.api.Test

class GetAwardCriteriaResponseTest :
    AbstractDTOTestBase<GetAwardCriteriaResponse>(GetAwardCriteriaResponse::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/get/awardCriteria/response/response_get_award_criteria.json")
    }
}
