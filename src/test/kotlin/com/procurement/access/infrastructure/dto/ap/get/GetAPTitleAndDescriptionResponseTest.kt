package com.procurement.access.infrastructure.dto.ap.get

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.GetAPTitleAndDescriptionResponse
import org.junit.jupiter.api.Test

class GetAPTitleAndDescriptionResponseTest : AbstractDTOTestBase<GetAPTitleAndDescriptionResponse>(
    GetAPTitleAndDescriptionResponse::class.java
) {

    @Test
    fun test() {
        testBindingAndMapping("json/dto/ap/get/get_ap_title_and_description_response_full.json")
    }
}
