package com.procurement.access.infrastructure.bind.money

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.procurement.access.domain.model.money.Money
import java.math.BigDecimal
import java.math.RoundingMode

class MoneyDeserializer : JsonDeserializer<Money>() {
    companion object {
        fun deserialize(money: ObjectNode): Money {
            val amount: BigDecimal = money.get("amount").decimalValue()
                .setScale(2, RoundingMode.HALF_UP)
            val currency: String = money.get("currency").asText()
            return Money(amount = amount, currency = currency)
        }
    }

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Money {
        val moneyNode = jsonParser.readValueAsTree<ObjectNode>()
        return deserialize(moneyNode)
    }
}
