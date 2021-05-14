package com.procurement.access.infrastructure.dto.responder

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.response.PersonesProcessingResult
import org.junit.jupiter.api.Test

class PersonesProcessingResponseTest : AbstractDTOTestBase<PersonesProcessingResult>(
    PersonesProcessingResult::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/processing/response/response_persones_processing_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/processing/response/response_persones_processing_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/processing/response/response_persones_processing_required_2.json")
    }
}
