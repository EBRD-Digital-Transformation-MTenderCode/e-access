package com.procurement.access.infrastructure.dto.rfq.create.relation

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.response.CreateRelationToContractProcessStageResult
import org.junit.jupiter.api.Test

class CreateRelationToContractProcessStageResultTest : AbstractDTOTestBase<CreateRelationToContractProcessStageResult>(
    CreateRelationToContractProcessStageResult::class.java) {

    @Test
    fun full() {
        testBindingAndMapping("json/dto/rfq/create/relation/response_create_relation_to_contract_process_stage_full.json")
    }
}
