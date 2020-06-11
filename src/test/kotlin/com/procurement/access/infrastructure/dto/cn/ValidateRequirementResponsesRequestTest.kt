package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.validate.ValidateRequirementResponsesRequest
import org.junit.jupiter.api.Test

class ValidateRequirementResponsesRequestTest : AbstractDTOTestBase<ValidateRequirementResponsesRequest>(
    ValidateRequirementResponsesRequest::class.java
) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/validate/requirement/validate_requirement_responses_request_full.json")
    }
}