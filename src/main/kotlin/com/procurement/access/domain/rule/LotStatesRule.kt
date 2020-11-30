package com.procurement.access.domain.rule

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails

class LotStatesRule(states: List<State>) : List<LotStatesRule.State> by states {

    data class State(
        @field:JsonProperty("status") @param:JsonProperty("status") val status: LotStatus,
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: LotStatusDetails
    )
}
