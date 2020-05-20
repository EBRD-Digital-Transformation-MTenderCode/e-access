package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.responder.processing.ResponderProcessing
import com.procurement.access.infrastructure.handler.processing.responder.ResponderProcessingResult

fun ResponderProcessing.Params.Responder.toReference(): ResponderProcessingResult =
    ResponderProcessingResult( // FR-10.1.4.7
        name = this.name,  // FR-10.1.4.8
        identifier = this.identifier // FR-10.1.4.9
            .let { identifier ->
                ResponderProcessingResult.Identifier(
                    id = identifier.id, // FR-10.1.4.10
                    scheme = identifier.scheme // FR-10.1.4.11
                )
            }
    )

