package com.procurement.access.domain.model.money

import java.math.BigDecimal

data class Money(val amount: BigDecimal, val currency: String) {
    operator fun plus(other: Money): Money? =
        if (currency == other.currency)
            Money(amount = amount + other.amount, currency = currency)
        else
            null
}
