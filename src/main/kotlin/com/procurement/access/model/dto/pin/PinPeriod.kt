package com.procurement.access.model.dto.pin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PinPeriod @JsonCreator constructor(

        val startDate: LocalDateTime,

        val endDate: LocalDateTime?
)