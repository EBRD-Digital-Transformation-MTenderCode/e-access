package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OrganizationReference @JsonCreator constructor(

        var id: String?,

        val name: String,

        @field:Valid
        val identifier: Identifier,

        @field:Valid
        val address: Address?,

        @field:Valid
        val additionalIdentifiers: HashSet<Identifier>?,

        @field:Valid
        val contactPoint: ContactPoint?
)
