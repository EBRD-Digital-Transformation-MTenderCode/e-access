package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.NegotiationCnOnPnResponse
import org.junit.jupiter.api.Test

class NegotiationCnOnPnResponseTest :
    AbstractDTOTestBase<NegotiationCnOnPnResponse>(NegotiationCnOnPnResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/create/cn_on_pn/lp/response/response_cn_on_pn_full.json")
    }

    @Test
    fun required() {
        testBindingAndMapping("json/dto/create/cn_on_pn/lp/response/response_cn_on_pn_required.json")
    }
}
