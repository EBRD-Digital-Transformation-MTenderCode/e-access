package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PnPeriod @JsonCreator constructor(

        @field:NotNull
        val startDate: LocalDateTime,

        val endDate: LocalDateTime?
)

