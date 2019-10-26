package com.procurement.access.infrastructure.bind.money

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.procurement.access.domain.model.money.Money

class MoneySerializer : JsonSerializer<Money>() {
    companion object {
        fun serialize(money: Money): ObjectNode = JsonNodeFactory.withExactBigDecimals(true)
            .objectNode()
            .apply {
                put("amount", money.amount)
                put("currency", money.currency)
            }
    }

    override fun serialize(money: Money, jsonGenerator: JsonGenerator, provider: SerializerProvider) =
        jsonGenerator.writeTree(serialize(money))
}
