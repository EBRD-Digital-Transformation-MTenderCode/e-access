package com.procurement.access.infrastructure.dto.cn.update

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.UpdateOpenCnResponse
import org.junit.jupiter.api.Test

class UpdateOpenCnResponseTest : AbstractDTOTestBase<UpdateOpenCnResponse>(UpdateOpenCnResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/update/cn/op/response/response_update_cn_op_fully.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/update/cn/op/response/response_update_cn_op_required_1.json")
    }
}
