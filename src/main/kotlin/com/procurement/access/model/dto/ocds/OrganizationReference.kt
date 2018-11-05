package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OrganizationReference @JsonCreator constructor(

        var id: String?,

        val name: String,

        val identifier: Identifier,

        val address: Address?,

        val additionalIdentifiers: HashSet<Identifier>?,

        val contactPoint: ContactPoint?
)
