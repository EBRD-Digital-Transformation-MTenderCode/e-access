package com.procurement.access.model.dto.pin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.Planning
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PinProcess @JsonCreator constructor(

        var ocid: String?,

        var token: String?,

        @field:Valid @field:NotNull
        var planning: Planning,

        @field:Valid @field:NotNull
        val tender: PinTender
)
