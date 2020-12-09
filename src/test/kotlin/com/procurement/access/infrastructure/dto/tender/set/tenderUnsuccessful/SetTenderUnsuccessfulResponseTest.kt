package com.procurement.access.infrastructure.dto.tender.set.tenderUnsuccessful

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.SetTenderUnsuccessfulResponse
import org.junit.jupiter.api.Test

class SetTenderUnsuccessfulResponseTest :
    AbstractDTOTestBase<SetTenderUnsuccessfulResponse>(SetTenderUnsuccessfulResponse::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/set/tenderUnsuccessful/response/response_set_tender_unsuccessful.json")
    }
}
