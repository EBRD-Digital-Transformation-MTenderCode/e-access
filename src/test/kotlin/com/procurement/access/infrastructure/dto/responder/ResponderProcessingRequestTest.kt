package com.procurement.access.infrastructure.dto.responder

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.v2.model.request.ResponderProcessingRequest
import org.junit.jupiter.api.Test

class ResponderProcessingRequestTest : AbstractDTOTestBase<ResponderProcessingRequest.Params>(
    ResponderProcessingRequest.Params::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/processing/request/request_responder_processing_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/processing/request/request_responder_processing_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/processing/request/request_responder_processing_required_2.json")
    }
}
