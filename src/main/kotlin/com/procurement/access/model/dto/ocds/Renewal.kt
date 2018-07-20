package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.model.dto.databinding.BooleansDeserializer
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Renewal @JsonCreator constructor(

        @field:NotNull
        @field:JsonDeserialize(using = BooleansDeserializer::class)
        @get:JsonProperty("hasRenewals")
        val hasRenewals: Boolean?
)
