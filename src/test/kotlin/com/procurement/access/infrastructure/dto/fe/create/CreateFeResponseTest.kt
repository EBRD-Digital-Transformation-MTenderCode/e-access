package com.procurement.access.infrastructure.dto.fe.create

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.CreateFEResponse
import org.junit.jupiter.api.Test

class CreateFeResponseTest : AbstractDTOTestBase<CreateFEResponse>(CreateFEResponse::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/dto/fe/create/response/response_create_fe_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/fe/create/response/response_create_fe_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/fe/create/response/response_create_fe_required_2.json")
    }
}
