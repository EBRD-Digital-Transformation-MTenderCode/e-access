package com.procurement.access.infrastructure.dto.criteria

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.FindCriteriaRequest
import org.junit.jupiter.api.Test

class FindCriteriaRequestTest : AbstractDTOTestBase<FindCriteriaRequest>(
    FindCriteriaRequest::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/find/criteria/request_find_criteria_full.json")
    }
}
