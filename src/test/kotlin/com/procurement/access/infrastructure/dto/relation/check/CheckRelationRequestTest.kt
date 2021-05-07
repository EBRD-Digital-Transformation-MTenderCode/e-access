package com.procurement.access.infrastructure.dto.relation.check

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.CheckRelationRequest
import org.junit.jupiter.api.Test

class CheckRelationRequestTest : AbstractDTOTestBase<CheckRelationRequest>(CheckRelationRequest::class.java) {

    @Test
    fun test() {
        testBindingAndMapping("json/dto/check/relation/check_relationg_request_full.json")
    }

    @Test
    fun required() {
        testBindingAndMapping("json/dto/check/relation/check_relationg_request_required.json")
    }
}
