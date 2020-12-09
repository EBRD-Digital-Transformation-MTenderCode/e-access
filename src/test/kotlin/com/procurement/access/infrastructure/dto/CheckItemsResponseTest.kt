package com.procurement.access.infrastructure.dto

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.CheckItemsResponse
import org.junit.jupiter.api.Test

class CheckItemsResponseTest : AbstractDTOTestBase<CheckItemsResponse>(CheckItemsResponse::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/check/items/response/response_check_Items.json")
    }
}
