package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CnOnPnRequestTest : AbstractDTOTestBase<CnOnPnRequest>(CnOnPnRequest::class.java) {
    @Test
    fun fully() {
        testBindingAndMapping("json/dto/create/cn_on_pn/op/request/request_cn_on_pn_full.json")
    }

    @Test
    fun required() {
        testBindingAndMapping("json/dto/create/cn_on_pn/op/request/request_cn_on_pn_required.json")
    }
}
