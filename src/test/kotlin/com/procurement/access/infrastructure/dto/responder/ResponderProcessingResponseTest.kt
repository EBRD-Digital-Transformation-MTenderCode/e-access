package com.procurement.access.infrastructure.dto.responder

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.processing.responder.ResponderProcessingResponse
import org.junit.jupiter.api.Test

class ResponderProcessingResponseTest : AbstractDTOTestBase<ResponderProcessingResponse>(
    ResponderProcessingResponse::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/processing/response/response_responder_processing_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/processing/response/response_responder_processing_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/processing/response/response_responder_processing_required_2.json")
    }

    @Test
    fun required3() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/processing/response/response_responder_processing_required_3.json")
    }
}
