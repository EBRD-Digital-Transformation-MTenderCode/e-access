package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CnOnPnResponseTest : AbstractDTOTestBase<CnOnPnResponse>(CnOnPnResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/create/cn_on_pn/op/response/response_cn_on_pn_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/create/cn_on_pn/op/response/response_cn_on_pn_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/create/cn_on_pn/op/response/response_cn_on_pn_required_2.json")
    }
}
