package com.procurement.notice.databinding

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

class MoneyDeserializer : JsonDeserializer<BigDecimal>() {

    private val delegate = NumberDeserializers.BigDecimalDeserializer.instance

    @Throws(IOException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): BigDecimal {
        var bd = delegate.deserialize(jsonParser, deserializationContext)
        bd = bd.setScale(2, RoundingMode.HALF_UP)
        return bd
    }
}