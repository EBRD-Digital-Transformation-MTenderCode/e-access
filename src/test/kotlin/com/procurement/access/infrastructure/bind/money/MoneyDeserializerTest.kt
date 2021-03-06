package com.procurement.access.infrastructure.bind.money

import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.access.domain.model.money.Money
import com.procurement.access.infrastructure.bind.jackson.configuration
import com.procurement.access.infrastructure.exception.MoneyParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

class MoneyDeserializerTest {
    companion object {
        private val mapper = ObjectMapper()
            .apply {
                this.configuration()
            }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            """{ "amount": 0, "currency": "MDL" };      0""",
            """{ "amount": 0.0, "currency": "MDL" };    0.0""",
            """{ "amount": 0.00, "currency": "MDL" };   0.00""",
            """{ "amount": 100, "currency": "MDL" };    100""",
            """{ "amount": 100.0, "currency": "MDL" };  100.0""",
            """{ "amount": 100.00, "currency": "MDL" }; 100.00""",
            """{ "amount": 100.1, "currency": "MDL" };  100.1""",
            """{ "amount": 100.15, "currency": "MDL" }; 100.15""",
            """{ "amount": 100.05, "currency": "MDL" }; 100.05"""
        ],
        delimiter = ';'
    )
    fun success(json: String, expected: String) {
        val money: Money = mapper.readValue(json, Money::class.java)

        assertEquals(BigDecimal(expected), money.amount)
        assertEquals("MDL", money.currency)
    }

    @ParameterizedTest(name = "{1}")
    @CsvSource(
        value = [
            """{ "currency": "MDL" };                     The attribute 'amount' is missing.""",
            """{ "amount": -100.01, "currency": "MDL" };  The amount must not be negative.""",
            """{ "amount": 100.015, "currency": "MDL" };  Attribute 'amount' is an invalid scale '3', the maximum scale: '2'.""",
            """{ "amount": "100.01", "currency": "MDL" }; Attribute 'amount' is an invalid type 'STRING', the required type is number.""",
            """{ "amount": true, "currency": "MDL" };     Attribute 'amount' is an invalid type 'BOOLEAN', the required type is number.""",
            """{ "amount": null, "currency": "MDL" };     Attribute 'amount' is an invalid type 'NULL', the required type is number."""
        ],
        delimiter = ';'
    )
    fun amount(json: String, textError: String) {
        val exception: MoneyParseException = assertThrows {
            mapper.readValue(json, Money::class.java)
        }

        assertEquals(textError, exception.message)
    }

    @ParameterizedTest(name = "{1}")
    @CsvSource(
        value = [
            """{ "amount": 100.01 };                   The attribute 'currency' is missing.""",
            """{ "amount": 100.01, "currency": 100 };  Attribute 'currency' is an invalid type 'NUMBER', the required type is text.""",
            """{ "amount": 100.01, "currency": true }; Attribute 'currency' is an invalid type 'BOOLEAN', the required type is text.""",
            """{ "amount": 100.01, "currency": null }; Attribute 'currency' is an invalid type 'NULL', the required type is text."""
        ],
        delimiter = ';'
    )
    fun currency(json: String, textError: String) {
        val exception: MoneyParseException = assertThrows {
            mapper.readValue(json, Money::class.java)
        }
        assertEquals(textError, exception.message)
    }
}
