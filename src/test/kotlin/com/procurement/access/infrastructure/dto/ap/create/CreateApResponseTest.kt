package com.procurement.access.infrastructure.dto.ap.create

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CreateApResponseTest : AbstractDTOTestBase<ApCreateResponse>(
    ApCreateResponse::class.java) {
    @Test
    fun fully() {
        testBindingAndMapping("json/dto/ap/create/response/response_create_ap_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/ap/create/response/response_create_ap_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/ap/create/response/response_create_ap_required_2.json")
    }
}
