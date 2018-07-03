package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Identifier @JsonCreator constructor(

        @field:NotNull
        val id: String,

        @field:NotNull
        val scheme: String,

        @field:NotNull
        val legalName: String,

        @field:NotNull
        val uri: String
)