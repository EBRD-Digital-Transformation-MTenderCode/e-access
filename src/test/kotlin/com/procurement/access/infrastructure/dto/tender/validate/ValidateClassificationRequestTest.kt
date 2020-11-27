package com.procurement.access.infrastructure.dto.tender.validate

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.ValidateClassificationRequest
import org.junit.jupiter.api.Test

class ValidateClassificationRequestTest
    : AbstractDTOTestBase<ValidateClassificationRequest>(ValidateClassificationRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/validate/request_validate_classification_full.json")
    }
}
