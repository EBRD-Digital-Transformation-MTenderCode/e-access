package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.validate.ValidateRequirementResponsesResult
import org.junit.jupiter.api.Test

class ValidateRequirementResponsesResultTest : AbstractDTOTestBase<ValidateRequirementResponsesResult>(
    ValidateRequirementResponsesResult::class.java
) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/validate/requirement/validate_requirement_responses_result_full.json")
    }
}