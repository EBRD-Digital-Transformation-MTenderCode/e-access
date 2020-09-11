package com.procurement.access.infrastructure.dto.ap

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CreateApResponseTest : AbstractDTOTestBase<ApCreateRequest>(ApCreateRequest::class.java) {
    @Test
    fun fully() {
        testBindingAndMapping("json/dto/ap/create/request/request_create_ap_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/ap/create/request/request_create_ap_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/ap/create/request/request_create_ap_required_2.json")
    }
}
