package com.procurement.access.domain.money

import com.procurement.access.domain.model.money.Money
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

class MoneyTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            """0;      0""",
            """0.0;    0.0""",
            """0.00;   0.00""",
            """100;    100""",
            """100.0;  100.0""",
            """100.00; 100.00""",
            """100.1;  100.1""",
            """100.15; 100.15""",
            """100.05; 100.05"""
        ],
        delimiter = ';'
    )
    fun `Test success amount contract`(amount: String, expected: String) {
        val money = Money(
            amount = BigDecimal(amount),
            currency = "MDL"
        )

        val expectedValue = BigDecimal(expected)
        assertEquals(expectedValue, money.amount)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            """100.157; The 'amount' is an invalid scale '3', the maximum scale: '${Money.AVAILABLE_SCALE}'."""
        ],
        delimiter = ';'
    )
    fun `Test fail amount contract`(amount: String, textError: String) {
        val exception = assertThrows<IllegalArgumentException> {
            Money(
                amount = BigDecimal(amount),
                currency = "MDL"
            )
        }

        assertEquals(textError, exception.message)
    }

    @Test
    fun `Test equals contract`() {
        EqualsVerifier.forClass(Money::class.java)
            .verify()
    }
}
