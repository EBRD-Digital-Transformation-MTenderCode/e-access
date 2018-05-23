package com.procurement.access.model.dto.pin

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.Planning
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(value = JsonInclude.Include.NON_NULL)
data class PinProcess(

        @JsonProperty("token")
        var token: String?,

        @JsonProperty("ocid")
        var ocId: String?,

        @JsonProperty("planning") @Valid @NotNull
        var planning: Planning,

        @JsonProperty("tender") @Valid @NotNull
        val tender: PinTender
)
