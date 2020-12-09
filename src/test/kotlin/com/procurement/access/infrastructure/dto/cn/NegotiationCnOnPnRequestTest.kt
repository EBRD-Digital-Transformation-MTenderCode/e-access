package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.request.NegotiationCnOnPnRequest
import org.junit.jupiter.api.Test

class NegotiationCnOnPnRequestTest :
    AbstractDTOTestBase<NegotiationCnOnPnRequest>(NegotiationCnOnPnRequest::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/create/cn_on_pn/lp/request/request_cn_on_pn_full.json")
    }

    @Test
    fun required() {
        testBindingAndMapping("json/dto/create/cn_on_pn/lp/request/request_cn_on_pn_required.json")
    }
}
