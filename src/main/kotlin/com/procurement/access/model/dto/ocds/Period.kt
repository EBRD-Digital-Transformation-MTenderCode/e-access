package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Period @JsonCreator constructor(

        @field:NotNull
        val startDate: LocalDateTime,

        @field:NotNull
        val endDate: LocalDateTime
)
