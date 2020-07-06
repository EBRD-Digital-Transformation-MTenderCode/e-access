package com.procurement.access.model.dto.state

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails

data class States(val states: List<State>) : List<States.State> by states {

    data class State(
        @field:JsonProperty("status") @param:JsonProperty("status") val status: TenderStatus,
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: TenderStatusDetails
    )
}
