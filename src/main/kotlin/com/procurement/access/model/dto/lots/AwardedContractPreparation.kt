package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.*

data class AwardedContractPreparationRq @JsonCreator constructor(

        val unsuccessfulLots: HashSet<UpdateLotDto>?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AwardedContractPreparationRs @JsonCreator constructor(

        val tender: Tender
)