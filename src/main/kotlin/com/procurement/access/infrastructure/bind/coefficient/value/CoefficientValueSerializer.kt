package com.procurement.access.infrastructure.bind.coefficient.value

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.procurement.access.domain.model.coefficient.CoefficientValue
import java.io.IOException

class CoefficientValueSerializer : JsonSerializer<CoefficientValue>() {
    companion object {
        fun serialize(coefficientValue: CoefficientValue.AsString): String = coefficientValue.value
        fun serialize(coefficientValue: CoefficientValue.AsBoolean): Boolean = coefficientValue.value
        fun serialize(coefficientValue: CoefficientValue.AsNumber): String = "%.2f".format(coefficientValue.value)
        fun serialize(coefficientValue: CoefficientValue.AsInteger): Int = coefficientValue.value
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(
        coefficientValue: CoefficientValue,
        jsonGenerator: JsonGenerator,
        provider: SerializerProvider
    ) =
        when (coefficientValue) {
            is CoefficientValue.AsString -> jsonGenerator.writeString(serialize(coefficientValue))
            is CoefficientValue.AsNumber -> jsonGenerator.writeNumber(serialize(coefficientValue))
            is CoefficientValue.AsBoolean -> jsonGenerator.writeBoolean(serialize(coefficientValue))
            is CoefficientValue.AsInteger -> jsonGenerator.writeNumber(serialize(coefficientValue))
        }
}
