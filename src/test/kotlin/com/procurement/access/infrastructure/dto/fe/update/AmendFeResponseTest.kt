package com.procurement.access.infrastructure.dto.fe.update

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class AmendFeResponseTest : AbstractDTOTestBase<AmendFEResponse>(AmendFEResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/fe/amend/response/response_amend_fe_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/fe/amend/response/response_amend_fe_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/fe/amend/response/response_amend_fe_required_2.json")
    }
}
