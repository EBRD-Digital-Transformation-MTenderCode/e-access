package com.procurement.access.model.dto.databinding

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

class MoneyDeserializer : JsonDeserializer<BigDecimal>() {

    private val delegate = NumberDeserializers.BigDecimalDeserializer.instance

    @Throws(IOException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): BigDecimal {
        if (jsonParser.currentToken == JsonToken.VALUE_STRING) {
            throw ErrorException(ErrorType.INVALID_JSON_TYPE, jsonParser.currentName)
        }
        var bd = delegate.deserialize(jsonParser, deserializationContext)
        bd = bd.setScale(2, RoundingMode.HALF_UP)
        if (bd <= BigDecimal.valueOf(0.00)) throw ErrorException(ErrorType.INVALID_JSON_TYPE, jsonParser.currentName)
        return bd
    }
}