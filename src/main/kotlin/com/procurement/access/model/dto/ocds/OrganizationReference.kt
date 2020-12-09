package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.infrastructure.entity.CNEntity

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OrganizationReference @JsonCreator constructor(

    var id: String?,

    val name: String,

    val identifier: Identifier,

    val address: Address?,

    val additionalIdentifiers: List<Identifier>?,

    val contactPoint: ContactPoint?,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("persones") @param:JsonProperty("persones") val persones: List<CNEntity.Tender.ProcuringEntity.Persone> = emptyList()
)
