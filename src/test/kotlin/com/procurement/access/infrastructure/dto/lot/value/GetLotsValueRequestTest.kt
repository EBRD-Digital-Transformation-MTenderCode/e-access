package com.procurement.access.infrastructure.dto.lot.value

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.GetLotsValueRequest
import org.junit.jupiter.api.Test

class GetLotsValueRequestTest : AbstractDTOTestBase<GetLotsValueRequest>(GetLotsValueRequest::class.java) {

    @Test
    fun test() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/value/request_get_lots_value.json")
    }
}