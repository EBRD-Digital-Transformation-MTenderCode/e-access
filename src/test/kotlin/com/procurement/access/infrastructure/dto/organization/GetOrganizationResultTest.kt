package com.procurement.access.infrastructure.dto.organization

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.get.organization.GetOrganizationResult
import org.junit.jupiter.api.Test

class GetOrganizationResultTest : AbstractDTOTestBase<GetOrganizationResult>(
    GetOrganizationResult::class.java
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
