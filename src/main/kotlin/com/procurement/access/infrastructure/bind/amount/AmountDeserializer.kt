package com.procurement.access.infrastructure.bind.amount

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.procurement.access.infrastructure.exception.AmountValueException
import java.io.IOException
import java.math.BigDecimal

class AmountDeserializer : JsonDeserializer<BigDecimal>() {
    companion object {
        fun deserialize(text: String): BigDecimal {
            val amount = try {
                BigDecimal(text)
            } catch (exception: Exception) {
                throw AmountValueException(text, exception.message ?: "")
            }

            if (amount <= BigDecimal.ZERO)
                throw IllegalArgumentException("The value less then zero.")
            return amount
        }
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): BigDecimal {
        if (jsonParser.currentToken != JsonToken.VALUE_NUMBER_FLOAT) {
            throw AmountValueException(
                amount = "\"${jsonParser.text}\"",
                description = "The value must be a real number."
            )
        }
        return deserialize(jsonParser.text)
    }
}
