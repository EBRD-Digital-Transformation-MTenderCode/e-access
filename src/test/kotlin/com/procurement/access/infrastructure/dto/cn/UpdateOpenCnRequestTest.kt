package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class UpdateOpenCnRequestTest : AbstractDTOTestBase<UpdateOpenCnRequest>(UpdateOpenCnRequest::class.java) {
    @Test
    fun fully() {
        testBindingAndMapping("json/dto/update/cn/ssp/request/request_update_cn_ssp_full.json")
    }

    @Test
    fun required_1() {
        testBindingAndMapping("json/dto/update/cn/ssp/request/request_update_cn_ssp_required_1.json")
    }

    @Test
    fun required_2() {
        testBindingAndMapping("json/dto/update/cn/ssp/request/request_update_cn_ssp_required_2.json")
    }

    @Test
    fun required_3() {
        testBindingAndMapping("json/dto/update/cn/ssp/request/request_update_cn_ssp_required_3.json")
    }
}
