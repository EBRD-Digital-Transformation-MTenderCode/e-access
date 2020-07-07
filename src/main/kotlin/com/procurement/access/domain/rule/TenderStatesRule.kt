package com.procurement.access.domain.rule

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails

class TenderStatesRule(states: List<State>) : List<TenderStatesRule.State> by states {

    data class State(
        @field:JsonProperty("status") @param:JsonProperty("status") val status: TenderStatus,
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: TenderStatusDetails
    )
}
