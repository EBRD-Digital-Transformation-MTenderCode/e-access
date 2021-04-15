package com.procurement.access.infrastructure.dto.organization

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.response.GetOrganizationsResult
import org.junit.jupiter.api.Test

class GetOrganizationsResultTest : AbstractDTOTestBase<GetOrganizationsResult>(
    GetOrganizationsResult::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/get/organization/response/response_get_organization_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/get/organization/response/response_get_organization_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/get/organization/response/response_get_organization_required_2.json")
    }

    @Test
    fun required3() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/get/organization/response/response_get_organization_required_3.json")
    }
}
