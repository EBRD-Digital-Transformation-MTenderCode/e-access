package com.procurement.access.infrastructure.dto.responder

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.check.persons.CheckPersonesStructureRequest
import org.junit.jupiter.api.Test

class CheckPersonesStructureRequestTest : AbstractDTOTestBase<CheckPersonesStructureRequest.Params>(
    CheckPersonesStructureRequest.Params::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/check/structure/request/request_check_persones_structure_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/check/structure/request/request_check_persones_structure_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/check/structure/request/request_check_persones_structure_required_2.json")
    }
}
