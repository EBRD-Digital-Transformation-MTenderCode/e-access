package com.procurement.access.infrastructure.dto.tender.process

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.create.relation.CreateRelationToOtherProcessResult
import org.junit.jupiter.api.Test

class CreateRelationToOtherProcessResponseTest : AbstractDTOTestBase<CreateRelationToOtherProcessResult>(
    CreateRelationToOtherProcessResult::class.java
) {

    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/create/relation/create_relation_to_other_process_response_full.json")
    }
}
