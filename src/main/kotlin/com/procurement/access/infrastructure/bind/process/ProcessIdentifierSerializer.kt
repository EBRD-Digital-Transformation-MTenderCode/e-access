package com.procurement.access.infrastructure.bind.process

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.access.domain.model.process.RelatedProcessIdentifier

class ProcessIdentifierSerializer : JsonSerializer<RelatedProcessIdentifier>() {
    companion object {
        fun serialize(identifier: RelatedProcessIdentifier): String = identifier.value
    }

    override fun serialize(commandId: RelatedProcessIdentifier, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeString(serialize(commandId))
}
