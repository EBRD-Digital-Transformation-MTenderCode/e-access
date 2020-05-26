package com.procurement.access.infrastructure.dto.criteria

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.get.criteria.GetQualificationCriteriaAndMethodResult
import org.junit.jupiter.api.Test

class GetQualificationCriteriaAndMethodResultTest : AbstractDTOTestBase<GetQualificationCriteriaAndMethodResult>(
    GetQualificationCriteriaAndMethodResult::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/get/criteria/result_get_qualification_criteria_and_method_full.json")
    }
}
