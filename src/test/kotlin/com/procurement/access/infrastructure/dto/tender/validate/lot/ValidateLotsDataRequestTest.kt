package com.procurement.access.infrastructure.dto.tender.validate.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.ValidateLotsDataRequest
import org.junit.jupiter.api.Test

class ValidateLotsDataRequestTest
    : AbstractDTOTestBase<ValidateLotsDataRequest>(ValidateLotsDataRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/validate/lot/request_validate_lots_data_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/tender/validate/lot/request_validate_lots_data_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/tender/validate/lot/request_validate_lots_data_required_2.json")
    }
}
