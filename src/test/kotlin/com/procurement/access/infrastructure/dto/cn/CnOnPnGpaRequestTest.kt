package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CnOnPnGpaRequestTest : AbstractDTOTestBase<CnOnPnGpaRequest>(CnOnPnGpaRequest::class.java) {
    @Test
    fun fully() {
        testBindingAndMapping("json/dto/create/cn_on_pn/gpa/request/request_cn_on_pn_gpa_full.json")
    }

    @Test
    fun required_1() {
        testBindingAndMapping("json/dto/create/cn_on_pn/gpa/request/request_cn_on_pn_gpa_required_1.json")
    }

    @Test
    fun required_2() {
        testBindingAndMapping("json/dto/create/cn_on_pn/gpa/request/request_cn_on_pn_gpa_required_2.json")
    }
}
