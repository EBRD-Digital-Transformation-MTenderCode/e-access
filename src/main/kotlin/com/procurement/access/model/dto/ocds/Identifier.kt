package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Identifier @JsonCreator constructor(

        val id: String,

        val scheme: String,

        val legalName: String,

        val uri: String?
)