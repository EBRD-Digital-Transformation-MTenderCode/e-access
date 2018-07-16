package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class SourceParty @JsonCreator constructor(

        var id: String,

        @field:NotNull
        val name: String
)
