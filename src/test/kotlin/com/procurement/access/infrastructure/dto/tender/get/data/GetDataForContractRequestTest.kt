package com.procurement.access.infrastructure.dto.tender.get.data

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.GetDataForContractRequest
import org.junit.jupiter.api.Test

class GetDataForContractRequestTest :
    AbstractDTOTestBase<GetDataForContractRequest>(GetDataForContractRequest::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/get/data/request_get_data_for_contract.json")
    }
}
