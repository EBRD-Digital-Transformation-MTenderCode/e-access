package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.VerifyRequirementResponseRequest
import org.junit.jupiter.api.Test

class VerifyRequirementResponseRequestTest : AbstractDTOTestBase<VerifyRequirementResponseRequest.Params>(VerifyRequirementResponseRequest.Params::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/responder/verify/request/request_verify_requirement_response_full.json")
    }
}
