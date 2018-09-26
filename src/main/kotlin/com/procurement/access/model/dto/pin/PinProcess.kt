package com.procurement.access.model.dto.pin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.Planning

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PinProcess @JsonCreator constructor(

        var ocid: String?,

        var token: String?,

        var planning: Planning,

        val tender: PinTender
)
