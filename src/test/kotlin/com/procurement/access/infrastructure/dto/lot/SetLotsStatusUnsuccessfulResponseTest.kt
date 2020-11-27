package com.procurement.access.infrastructure.dto.lot

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v1.model.response.SetLotsStatusUnsuccessfulResponse
import org.junit.jupiter.api.Test

class SetLotsStatusUnsuccessfulResponseTest :
    AbstractDTOTestBase<SetLotsStatusUnsuccessfulResponse>(SetLotsStatusUnsuccessfulResponse::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/lot/response/response_lots_set_status_unsuccessful_full.json")
    }
}
