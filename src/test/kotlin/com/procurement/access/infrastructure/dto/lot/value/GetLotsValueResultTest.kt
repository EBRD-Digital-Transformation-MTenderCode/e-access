package com.procurement.access.infrastructure.dto.lot.value

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.GetLotsValueResult
import org.junit.jupiter.api.Test

class GetLotsValueResultTest : AbstractDTOTestBase<GetLotsValueResult>(GetLotsValueResult::class.java) {

    @Test
    fun test() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/value/result_get_lots_value.json")
    }
}