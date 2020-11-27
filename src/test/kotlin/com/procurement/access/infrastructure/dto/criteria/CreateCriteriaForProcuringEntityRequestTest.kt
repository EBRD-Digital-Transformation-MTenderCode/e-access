package com.procurement.access.infrastructure.dto.criteria

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.CreateCriteriaForProcuringEntityRequest
import org.junit.jupiter.api.Test

class CreateCriteriaForProcuringEntityRequestTest : AbstractDTOTestBase<CreateCriteriaForProcuringEntityRequest>(
    CreateCriteriaForProcuringEntityRequest::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/get/criteria/request_create_criteria_for_procuring_entity_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/get/criteria/request_create_criteria_for_procuring_entity_required_1.json")
    }
}
