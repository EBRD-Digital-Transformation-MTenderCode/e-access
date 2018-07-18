package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TenderProcess @JsonCreator constructor(

        val ocid: String?,

        var token: String?,

        val planning: Planning,

        val tender: Tender
)
