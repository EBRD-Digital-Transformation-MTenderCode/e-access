package com.procurement.access.infrastructure.dto.fe.update

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class UpdateFeRequestTest : AbstractDTOTestBase<UpdateFERequest>(UpdateFERequest::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/fe/update/request/request_update_fe_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/fe/update/request/request_update_fe_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/fe/update/request/request_update_fe_required_2.json")
    }
}
