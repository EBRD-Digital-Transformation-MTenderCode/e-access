package com.procurement.access.infrastructure.dto.pn.outsourcing

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.pn.OutsourcingPNResult
import org.junit.jupiter.api.Test

class OutsourcingPNResponseTest : AbstractDTOTestBase<OutsourcingPNResult>(OutsourcingPNResult::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/pn/outsourcing/outsourcing_pn_response_full.json")
    }
}
