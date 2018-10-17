package com.procurement.access.model.dto.tender

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateTenderStatusRs @JsonCreator constructor(

        val status: String?,

        val statusDetails: String?
)
