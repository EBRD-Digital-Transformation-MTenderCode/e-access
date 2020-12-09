package com.procurement.access.infrastructure.dto.ap.update

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.ApUpdateResponse
import org.junit.jupiter.api.Test

class UpdateApResponseTest : AbstractDTOTestBase<ApUpdateResponse>(ApUpdateResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/ap/update/response/response_update_ap_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/ap/update/response/response_update_ap_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/ap/update/response/response_update_ap_required_2.json")
    }

    @Test
    fun required3() {
        testBindingAndMapping("json/dto/ap/update/response/response_update_ap_required_3.json")
    }
}
