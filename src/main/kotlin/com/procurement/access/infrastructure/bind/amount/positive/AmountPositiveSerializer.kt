package com.procurement.access.infrastructure.bind.amount.positive

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException
import java.math.BigDecimal

class AmountPositiveSerializer : JsonSerializer<BigDecimal>() {
    companion object {
        fun serialize(amount: BigDecimal): String = "%.2f".format(amount)
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(amount: BigDecimal, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeNumber(serialize(amount))
}