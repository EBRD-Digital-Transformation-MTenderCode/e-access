package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class GetLotIdsRequestTest : AbstractDTOTestBase<GetLotIdsRequest>(GetLotIdsRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping(pathToJsonFile = "{\n  \"states\": [\n    {\n      \"status\": \"active\",\n      \"statusDetails\": \"empty\"\n    },\n    {\n      \"status\": \"active\"\n    },\n    {\n      \"statusDetails\": \"empty\"\n    }\n  ],\n  \"cpid\": \"ocds-b3wdp1-MD-1580458690892\",\n  \"ocid\": \"ocds-b3wdp1-MD-1580458690892-EV-1580458791896\"\n}")
    }

    @Test
    fun test1() {
        testBindingAndMapping(pathToJsonFile = "{\n  \"states\": [\n    {}\n  ],\n  \"cpid\": \"ocds-b3wdp1-MD-1580458690892\",\n  \"ocid\": \"ocds-b3wdp1-MD-1580458690892-EV-1580458791896\"\n}")
    }
}
