package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CancellationResponseDto @JsonCreator constructor(

        val lots: List<LotCancellation>,

        val tender: TenderCancellation
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LotCancellation @JsonCreator constructor(

        val id: String,

        var status: String? = null,

        var statusDetails: String?
)


@JsonInclude(JsonInclude.Include.NON_NULL)
data class TenderCancellation @JsonCreator constructor(

        var status: String? = null,

        var statusDetails: String?
)