package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder("id", "name", "identifier", "address", "additionalIdentifiers", "contactPoint")
data class OrganizationReference(

        @JsonProperty("id")
        var id: String?,

        @JsonProperty("name") @Size(min = 1)
        val name: String,

        @Valid
        @param:JsonProperty("identifier")
        val identifier: Identifier?,

        @Valid
        @JsonProperty("address")
        val address: Address?,

        @Valid
        @JsonProperty("additionalIdentifiers")
        val additionalIdentifiers: HashSet<Identifier>?,

        @Valid
        @JsonProperty("contactPoint")
        val contactPoint: ContactPoint?
)
