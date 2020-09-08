package com.procurement.access.infrastructure.dto.relation.check

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.check.relation.CheckRelationRequest
import org.junit.jupiter.api.Test

class CheckRelationRequestTest : AbstractDTOTestBase<CheckRelationRequest>(CheckRelationRequest::class.java) {

    @Test
    fun test() {
        testBindingAndMapping("json/dto/check/relation/check_relationg_request_full.json")
    }
}
