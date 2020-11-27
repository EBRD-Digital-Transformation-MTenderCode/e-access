package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.request.UpdateNegotiationCnRequest
import org.junit.jupiter.api.Test

class UpdateNegotiationCnRequestTest :
    AbstractDTOTestBase<UpdateNegotiationCnRequest>(UpdateNegotiationCnRequest::class.java) {
    @Test
    fun fully() {
        testBindingAndMapping("json/dto/update/cn/op/request/request_update_cn_op_full.json")
    }

    @Test
    fun required_1() {
        testBindingAndMapping("json/dto/update/cn/op/request/request_update_cn_op_required_1.json")
    }

    @Test
    fun required_2() {
        testBindingAndMapping("json/dto/update/cn/op/request/request_update_cn_op_required_2.json")
    }
}
