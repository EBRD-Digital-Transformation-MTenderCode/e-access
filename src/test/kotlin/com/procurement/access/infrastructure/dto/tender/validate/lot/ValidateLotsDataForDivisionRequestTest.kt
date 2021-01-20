package com.procurement.access.infrastructure.dto.tender.validate.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.ValidateLotsDataForDivisionRequest
import org.junit.jupiter.api.Test

class ValidateLotsDataForDivisionRequestTest
    : AbstractDTOTestBase<ValidateLotsDataForDivisionRequest>(ValidateLotsDataForDivisionRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/validate/lot/request_validate_lots_data_for_division_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/tender/validate/lot/request_validate_lots_data_for_division_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/tender/validate/lot/request_validate_lots_data_for_division_required_2.json")
    }
}
