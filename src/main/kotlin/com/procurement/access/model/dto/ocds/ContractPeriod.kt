package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ContractPeriod @JsonCreator constructor(

        val startDate: LocalDateTime,

        val endDate: LocalDateTime
)
