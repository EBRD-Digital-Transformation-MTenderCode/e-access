package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.domain.model.money.Money
import com.procurement.access.model.dto.databinding.MoneyDeserializer
import java.math.BigDecimal

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Value @JsonCreator constructor(

    @field:JsonDeserialize(using = MoneyDeserializer::class)
    var amount: BigDecimal,

    var currency: String
)

val Money.asValue: Value
    get() = this.let { money ->
        Value(
            amount = money.amount,
            currency = money.currency
        )
    }

val Value.asMoney: Money
    get() = this.let { value ->
        Money(
            amount = value.amount,
            currency = value.currency
        )
    }