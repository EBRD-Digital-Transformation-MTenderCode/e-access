package com.procurement.access.infrastructure.bind.money

import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.access.domain.model.money.Money
import com.procurement.access.infrastructure.bind.jackson.configuration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

class MoneySerializerTest {
    companion object {
        private val mapper = ObjectMapper().apply {
            this.configuration()
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            """0;      {"amount":0.00,"currency":"MDL"}""",
            """0.0;    {"amount":0.00,"currency":"MDL"}""",
            """0.00;   {"amount":0.00,"currency":"MDL"}""",
            """100;    {"amount":100.00,"currency":"MDL"}""",
            """100.0;  {"amount":100.00,"currency":"MDL"}""",
            """100.00; {"amount":100.00,"currency":"MDL"}""",
            """100.1;  {"amount":100.10,"currency":"MDL"}""",
            """100.15; {"amount":100.15,"currency":"MDL"}""",
            """100.05; {"amount":100.05,"currency":"MDL"}"""
        ],
        delimiter = ';'
    )
    fun success(amount: String, expected: String) {
        val money = Money(
            amount = BigDecimal(amount),
            currency = "MDL"
        )

        val result = mapper.writeValueAsString(money)

        assertEquals(expected, result)
    }
}
