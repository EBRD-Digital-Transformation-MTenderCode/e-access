package com.procurement.access.infrastructure.bind.money

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.procurement.access.domain.model.money.Money
import com.procurement.access.infrastructure.exception.MoneyParseException
import java.math.BigDecimal

class MoneyDeserializer : JsonDeserializer<Money>() {
    companion object {
        fun deserialize(node: ObjectNode): Money {
            if (!node.has("amount"))
                throw MoneyParseException("The attribute 'amount' is missing.")

            if (!node.has("currency"))
                throw MoneyParseException("The attribute 'currency' is missing.")

            val amountNode = node.get("amount")
            if (!amountNode.isNumber)
                throw MoneyParseException("Attribute 'amount' is an invalid type '${amountNode.nodeType.name}', the required type is number.")

            val currencyNode = node.get("currency")
            if (!currencyNode.isTextual)
                throw MoneyParseException("Attribute 'currency' is an invalid type '${currencyNode.nodeType.name}', the required type is text.")

            val amount: BigDecimal = amountNode.decimalValue()
            val scale = amount.scale()
            if (scale > Money.AVAILABLE_SCALE)
                throw MoneyParseException("Attribute 'amount' is an invalid scale '$scale', the maximum scale: '${Money.AVAILABLE_SCALE}'.")
            if (amount < BigDecimal.ZERO)
                throw MoneyParseException("The amount must not be negative.")

            val currency: String = currencyNode.asText()
            return Money(amount = amount, currency = currency)
        }
    }

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Money {
        val moneyNode = jsonParser.readValueAsTree<ObjectNode>()
        return deserialize(moneyNode)
    }
}
