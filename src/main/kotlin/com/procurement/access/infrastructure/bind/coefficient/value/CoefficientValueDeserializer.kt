package com.procurement.access.infrastructure.bind.coefficient.value

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.procurement.access.domain.model.coefficient.CoefficientValue
import com.procurement.access.infrastructure.exception.CoefficientValueException
import java.io.IOException
import java.math.BigDecimal

class CoefficientValueDeserializer : JsonDeserializer<CoefficientValue>() {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): CoefficientValue {
        return when (jsonParser.currentToken) {
            JsonToken.VALUE_STRING -> CoefficientValue.of(jsonParser.text)
            JsonToken.VALUE_FALSE -> CoefficientValue.of(false)
            JsonToken.VALUE_TRUE -> CoefficientValue.of(true)
            JsonToken.VALUE_NUMBER_INT -> CoefficientValue.of(jsonParser.longValue)
            JsonToken.VALUE_NUMBER_FLOAT -> CoefficientValue.of(BigDecimal(jsonParser.text))
            else -> throw CoefficientValueException(coefficientValue = jsonParser.text, description = "Incorrect type")
        }
    }
}
