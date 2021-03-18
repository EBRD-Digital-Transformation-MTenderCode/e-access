package com.procurement.access.infrastructure.dto.pn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.PnCreateResponse
import org.junit.jupiter.api.Test

class PnCreateResponseTest : AbstractDTOTestBase<PnCreateResponse>(PnCreateResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/create/pn/response/response_pn_full.json")
    }

    @Test
    fun required1() {
        val pathToJsonFile = "json/dto/create/pn/response/response_pn_required_1.json"
        testBindingAndMapping(pathToJsonFile)
    }

    @Test
    fun required2() {
        val pathToJsonFile = "json/dto/create/pn/response/response_pn_required_2.json"
        testBindingAndMapping(pathToJsonFile)
    }

    @Test
    fun required3() {
        val pathToJsonFile = "json/dto/create/pn/response/response_pn_required_3.json"
        testBindingAndMapping(pathToJsonFile)
    }
}
