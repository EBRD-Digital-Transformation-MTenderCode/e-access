package com.procurement.access.model.dto.bids

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.Value
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CheckBidDto @JsonCreator constructor(

    @field:Valid
    val value: Value,

    @field:NotEmpty
    var relatedLot: List<String>
)