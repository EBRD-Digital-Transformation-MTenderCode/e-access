package com.procurement.access.model.dto.tender

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GetTenderOwnerRs @JsonCreator constructor(

        val owner: String
)