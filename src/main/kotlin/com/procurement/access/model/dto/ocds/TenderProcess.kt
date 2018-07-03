package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TenderProcess @JsonCreator constructor(

        val ocid: String?,

        var token: String?,

        @field:Valid @field:NotNull
        val planning: Planning,

        @field:Valid @field:NotNull
        val tender: Tender
)
