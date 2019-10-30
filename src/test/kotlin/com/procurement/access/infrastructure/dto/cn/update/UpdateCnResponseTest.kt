package com.procurement.access.infrastructure.dto.cn.update

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class UpdateCnResponseTest : AbstractDTOTestBase<UpdateCnResponse>(
    UpdateCnResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/update/cn/op/response/response_update_cn_op.json")
    }
}
