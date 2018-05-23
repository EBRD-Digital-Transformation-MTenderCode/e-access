package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OrganizationReference(

        @JsonProperty("id")
        var id: String?,

        @JsonProperty("name") @Size(min = 1) @NotNull
        val name: String,

        @param:JsonProperty("identifier") @Valid
        val identifier: Identifier?,

        @JsonProperty("address") @Valid
        val address: Address?,

        @JsonProperty("additionalIdentifiers") @Valid
        val additionalIdentifiers: HashSet<Identifier>?,

        @JsonProperty("contactPoint") @Valid
        val contactPoint: ContactPoint?
)
