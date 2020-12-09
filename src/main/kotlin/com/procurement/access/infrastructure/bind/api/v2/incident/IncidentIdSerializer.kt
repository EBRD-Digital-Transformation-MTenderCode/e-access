package com.procurement.access.infrastructure.bind.api.v2.incident

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.access.infrastructure.api.v2.IncidentId
import java.io.IOException

class IncidentIdSerializer : JsonSerializer<IncidentId>() {
    companion object {
        fun serialize(id: IncidentId): String = id.underlying
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(id: IncidentId, jsonGenerator: JsonGenerator, provider: SerializerProvider) {
        jsonGenerator.writeString(serialize(id))
    }
}
