package com.procurement.access.infrastructure.bind.process

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.procurement.access.domain.model.process.RelatedProcessIdentifier

class ProcessIdentifierDeserializer : JsonDeserializer<RelatedProcessIdentifier>() {
    companion object {
        fun deserialize(text: String): RelatedProcessIdentifier = RelatedProcessIdentifier.create(text)
    }

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): RelatedProcessIdentifier =
        deserialize(jsonParser.text)
}
