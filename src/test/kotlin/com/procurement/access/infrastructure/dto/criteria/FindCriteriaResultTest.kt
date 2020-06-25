package com.procurement.access.infrastructure.dto.criteria

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.find.criteria.FindCriteriaResult
import org.junit.jupiter.api.Test

class FindCriteriaResultTest : AbstractDTOTestBase<FindCriteriaResult>(
    FindCriteriaResult::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/find/criteria/result_find_criteria_full.json")
    }

    @Test
    fun required_1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/find/criteria/result_find_criteria_required_1.json")
    }
}
