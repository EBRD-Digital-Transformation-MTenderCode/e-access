package com.procurement.access.infrastructure.dto.cn.update

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.dto.cn.UpdateSelectiveCnRequest
import org.junit.jupiter.api.Test

class UpdateSelectiveCnRequestTest : AbstractDTOTestBase<UpdateSelectiveCnRequest>(UpdateSelectiveCnRequest::class.java) {
    @Test
    fun fully() {
        testBindingAndMapping("json/dto/update/cn/selective/request/request_update_selective_cn_full.json")
    }

    @Test
    fun required_1() {
        testBindingAndMapping("json/dto/update/cn/selective/request/request_update_selective_cn_required_1.json")
    }

    @Test
    fun required_2() {
        testBindingAndMapping("json/dto/update/cn/selective/request/request_update_selective_cn_required_2.json")
    }

    @Test
    fun required_3() {
        testBindingAndMapping("json/dto/update/cn/selective/request/request_update_selective_cn_required_3.json")
    }
}
