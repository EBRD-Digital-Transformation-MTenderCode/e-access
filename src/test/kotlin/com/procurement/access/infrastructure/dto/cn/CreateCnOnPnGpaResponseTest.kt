package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CreateCnOnPnGpaResponseTest : AbstractDTOTestBase<CreateCnOnPnGpaResponse>(CreateCnOnPnGpaResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/create/cn_on_pn/selective/create/response/response_create_cn_on_pn_gpa_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/create/cn_on_pn/selective/create/response/response_create_cn_on_pn_gpa_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/create/cn_on_pn/selective/create/response/response_create_cn_on_pn_gpa_required_2.json")
    }
}
