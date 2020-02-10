package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class GetLotIdsResponseTest : AbstractDTOTestBase<GetLotIdsResponse>(GetLotIdsResponse::class.java) {
    @Test
    fun test() {
        testBindingAndMapping(pathToJsonFile = "{\n  \"result\": [\n    \"0f8544b3-04c4-4b21-9a52-09d643ce3d95\",\n    \"02d4c398-fa53-486b-9409-b04a60242755\"\n  ]\n}")
    }
}
