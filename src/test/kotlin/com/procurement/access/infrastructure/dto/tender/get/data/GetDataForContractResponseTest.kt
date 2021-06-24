package com.procurement.access.infrastructure.dto.tender.get.data

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.response.GetDataForContractResponse
import org.junit.jupiter.api.Test

class GetDataForContractResponseTest :
    AbstractDTOTestBase<GetDataForContractResponse>(GetDataForContractResponse::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/get/data/response_get_data_for_contract_full.json")
    }

    @Test
    fun test_required_1() {
        testBindingAndMapping("json/dto/tender/get/data/response_get_data_for_contract_required_1.json")
    }
}
