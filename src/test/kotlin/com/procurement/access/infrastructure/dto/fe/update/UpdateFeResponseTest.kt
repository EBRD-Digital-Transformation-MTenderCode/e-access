package com.procurement.access.infrastructure.dto.fe.update

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class UpdateFeResponseTest : AbstractDTOTestBase<UpdateFEResponse>(UpdateFEResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/fe/update/response/response_update_fe_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/fe/update/response/response_update_fe_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/fe/update/response/response_update_fe_required_2.json")
    }
}
