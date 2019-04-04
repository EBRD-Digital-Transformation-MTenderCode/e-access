package com.procurement.access.infrastructure.dto

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CheckItemsRequestTest : AbstractDTOTestBase<CheckItemsRequest>(CheckItemsRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/check/items/request/request_check_items.json")
    }
}
