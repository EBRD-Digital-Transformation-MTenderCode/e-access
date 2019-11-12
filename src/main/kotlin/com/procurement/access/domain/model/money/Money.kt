package com.procurement.access.domain.model.money

import java.math.BigDecimal
import kotlin.jvm.internal.Intrinsics

class Money private constructor(val amount: BigDecimal, val currency: String) {
    companion object {
        const val AVAILABLE_SCALE = 2

        operator fun invoke(amount: BigDecimal, currency: String): Money {
            checkScale(amount)
            return Money(amount = amount, currency = currency)
        }

        private fun checkScale(amount: BigDecimal) {
            val scale = amount.scale()
            require(scale <= AVAILABLE_SCALE) {
                "The 'amount' is an invalid scale '$scale', the maximum scale: '$AVAILABLE_SCALE'."
            }
        }
    }

    operator fun plus(other: Money): Money? =
        if (currency == other.currency)
            Money(amount = amount + other.amount, currency = currency)
        else
            null

    override fun equals(other: Any?): Boolean {
        return if (this !== other) {
            other is Money
                && Intrinsics.areEqual(this.amount, other.amount)
                && Intrinsics.areEqual(this.currency, other.currency)
        } else
            true
    }

    override fun hashCode(): Int {
        var result = currency.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }
}

inline fun <E : RuntimeException> Sequence<Money>.sum(notCompatibleCurrencyExceptionBuilder: () -> E): Money? =
    this.iterator().sum(notCompatibleCurrencyExceptionBuilder)

inline fun <E : RuntimeException> Iterable<Money>.sum(notCompatibleCurrencyExceptionBuilder: () -> E): Money? =
    this.iterator().sum(notCompatibleCurrencyExceptionBuilder)

inline fun <E : RuntimeException> Iterator<Money>.sum(notCompatibleCurrencyExceptionBuilder: () -> E): Money? {
    if (!this.hasNext()) return null
    val first: Money = this.next()
    var accumulator: BigDecimal = first.amount
    while (this.hasNext()) {
        val next: Money = this.next()
        if (first.currency != next.currency) throw notCompatibleCurrencyExceptionBuilder()
        accumulator += next.amount
    }
    return Money(amount = accumulator, currency = first.currency)
}
