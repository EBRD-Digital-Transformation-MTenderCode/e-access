package com.procurement.access.infrastructure.dto.responder

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.processing.responder.ResponderProcessingResult
import org.junit.jupiter.api.Test

class ResponderProcessingResponseTest : AbstractDTOTestBase<ResponderProcessingResult>(
    ResponderProcessingResult::class.java
) {
    @Test
    fun fully() {
        testBindingAndMapping(pathToJsonFile = "json/dto/responder/processing/response/response_responder_processing_full.json")
    }
}
