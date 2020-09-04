package com.procurement.access.infrastructure.dto.pn.outsourcing

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.pn.OutsourcingPNRequest
import org.junit.jupiter.api.Test

class OutsourcingPNRequestTest : AbstractDTOTestBase<OutsourcingPNRequest>(OutsourcingPNRequest::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/pn/outsourcing/outsourcing_pn_request_full.json")
    }
}
