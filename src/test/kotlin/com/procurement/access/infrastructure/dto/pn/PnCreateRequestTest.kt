package com.procurement.access.infrastructure.dto.pn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.request.PnCreateRequest
import org.junit.jupiter.api.Test

class PnCreateRequestTest : AbstractDTOTestBase<PnCreateRequest>(PnCreateRequest::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/create/pn/request/request_pn_full.json")
    }

    @Test
    fun required1() {
        val pathToJsonFile = "json/dto/create/pn/request/request_pn_required_1.json"
        testBindingAndMapping(pathToJsonFile)
    }

    @Test
    fun required2() {
        val pathToJsonFile = "json/dto/create/pn/request/request_pn_required_2.json"
        testBindingAndMapping(pathToJsonFile)
    }
}
