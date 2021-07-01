package com.procurement.access.infrastructure.dto.tender.define

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.response.DefineTenderClassificationResult
import org.junit.jupiter.api.Test

class DefineTenderClassificationResponseTest : AbstractDTOTestBase<DefineTenderClassificationResult>(DefineTenderClassificationResult::class.java) {

    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/define/define_tender_classification_response.json")
    }
}
