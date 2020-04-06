package com.procurement.access.infrastructure.dto.responder

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.check.persons.CheckPersonsStructureRequest
import org.junit.jupiter.api.Test

class CheckPersonsStructureRequestTest : AbstractDTOTestBase<CheckPersonsStructureRequest.Params>(
    CheckPersonsStructureRequest.Params::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/check/structure/request/request_check_persons_structure_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/check/structure/request/request_check_persons_structure_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/check/structure/request/request_check_persons_structure_required_2.json")
    }
}
