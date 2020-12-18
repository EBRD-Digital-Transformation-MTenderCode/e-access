package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.request.SelectiveCnOnPnRequest
import org.junit.jupiter.api.Test

class SelectiveCnOnPnRequestTest : AbstractDTOTestBase<SelectiveCnOnPnRequest>(SelectiveCnOnPnRequest::class.java) {
    @Test
    fun fully() {
        testBindingAndMapping("json/dto/create/cn_on_pn/selective/request/request_selective_cn_on_pn_full.json")
    }

    @Test
    fun required_1() {
        testBindingAndMapping("json/dto/create/cn_on_pn/selective/request/request_selective_cn_on_pn_required_1.json")
    }

    @Test
    fun required_2() {
        testBindingAndMapping("json/dto/create/cn_on_pn/selective/request/request_selective_cn_on_pn_required_2.json")
    }

    @Test
    fun required_3() {
        testBindingAndMapping("json/dto/create/cn_on_pn/selective/request/request_selective_cn_on_pn_required_3.json")
    }

    @Test
    fun required_4() {
        testBindingAndMapping("json/dto/create/cn_on_pn/selective/request/request_selective_cn_on_pn_required_4.json")
    }
}
