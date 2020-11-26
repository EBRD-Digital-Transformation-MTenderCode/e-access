package com.procurement.access.infrastructure.bind.api.version

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.procurement.access.infrastructure.api.ApiVersion

class ApiVersionDeserializer : JsonDeserializer<ApiVersion>() {
    companion object {
        fun deserialize(text: String) = ApiVersion.orThrow(text) {
            IllegalAccessException("Invalid format of the api version. Expected: '${ApiVersion.pattern}', actual: '$text'.")
        }
    }

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): ApiVersion =
        deserialize(jsonParser.text)
}
