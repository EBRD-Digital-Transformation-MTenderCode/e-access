package com.procurement.access.infrastructure.dto.criteria

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.response.CreateCriteriaForProcuringEntityResult
import org.junit.jupiter.api.Test

class CreateCriteriaForProcuringEntityResultTest : AbstractDTOTestBase<CreateCriteriaForProcuringEntityResult>(
    CreateCriteriaForProcuringEntityResult::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/get/criteria/result_create_criteria_for_procuring_entity_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/tender/get/criteria/result_create_criteria_for_procuring_entity_required_1.json")
    }
}
