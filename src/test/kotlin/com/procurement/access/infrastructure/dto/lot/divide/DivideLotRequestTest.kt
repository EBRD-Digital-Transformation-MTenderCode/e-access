package com.procurement.access.infrastructure.dto.lot.divide

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.DivideLotRequest
import org.junit.jupiter.api.Test

class DivideLotRequestTest : AbstractDTOTestBase<DivideLotRequest>(
    DivideLotRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/request/divide/request_divide_lot_full.json")
    }

    @Test
    fun test1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/request/divide/request_divide_lot_required_1.json")
    }

    @Test
    fun test2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/request/divide/request_divide_lot_required_2.json")
    }

    @Test
    fun test3() {
        testBindingAndMapping(pathToJsonFile = "json/dto/lot/request/divide/request_divide_lot_required_3.json")
    }
}
