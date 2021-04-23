package com.procurement.access.infrastructure.dto.organization

import com.procurement.access.application.model.organization.GetOrganizations
import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class GetOrganizationsRequestTest : AbstractDTOTestBase<GetOrganizations.Params>(
    GetOrganizations.Params::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/get/organization/request/request_get_organization_full.json")
    }
}
