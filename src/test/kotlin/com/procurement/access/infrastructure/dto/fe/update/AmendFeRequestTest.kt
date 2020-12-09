package com.procurement.access.infrastructure.dto.fe.update

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.request.AmendFERequest
import org.junit.jupiter.api.Test

class AmendFeRequestTest : AbstractDTOTestBase<AmendFERequest>(AmendFERequest::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/fe/amend/request/request_amend_fe_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/fe/amend/request/request_amend_fe_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/fe/amend/request/request_amend_fe_required_2.json")
    }
}
