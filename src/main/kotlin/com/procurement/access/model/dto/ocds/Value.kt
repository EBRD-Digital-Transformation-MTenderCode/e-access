package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.databinding.MoneyDeserializer
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Value @JsonCreator constructor(

        @field:JsonDeserialize(using = MoneyDeserializer::class)
        var amount: BigDecimal,

        var currency: String
)
