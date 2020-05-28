package com.procurement.access.infrastructure.dto.criteria

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.get.criteria.GetQualificationCriteriaAndMethodRequest
import org.junit.jupiter.api.Test

class GetQualificationCriteriaAndMethodRequestTest : AbstractDTOTestBase<GetQualificationCriteriaAndMethodRequest>(
    GetQualificationCriteriaAndMethodRequest::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/get/criteria/request_get_qualification_criteria_and_method_full.json")
    }
}
