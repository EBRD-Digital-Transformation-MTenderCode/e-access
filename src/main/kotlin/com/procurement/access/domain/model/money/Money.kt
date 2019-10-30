package com.procurement.access.domain.model.money

import java.math.BigDecimal

data class Money(val amount: BigDecimal, val currency: String) {
    operator fun plus(other: Money): Money? =
        if (currency == other.currency)
            Money(amount = amount + other.amount, currency = currency)
        else
            null
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
