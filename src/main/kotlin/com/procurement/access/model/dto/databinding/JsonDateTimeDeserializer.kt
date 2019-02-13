package com.procurement.access.model.dto.databinding

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException
import java.time.LocalDateTime

class JsonDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): LocalDateTime =
        LocalDateTime.parse(jsonParser.text, JsonDateTimeFormatter.formatter)
}
