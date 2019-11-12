package com.procurement.access.infrastructure.bind.money

import com.fasterxml.jackson.databind.module.SimpleModule
import com.procurement.access.domain.model.money.Money

class MoneyModule : SimpleModule() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
    }

    init {
        addSerializer(Money::class.java, MoneySerializer())
        addDeserializer(Money::class.java, MoneyDeserializer())
    }
}
