package com.procurement.access.model.dto.tender

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.ElectronicAuctions
import com.procurement.access.model.dto.ocds.ProcurementMethodModalities

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UnsuspendedTenderRs @JsonCreator constructor(

        val tender: UnsuspendedTender
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UnsuspendedTender @JsonCreator constructor(

        val status: String?,

        val statusDetails: String?,

        val procurementMethodModalities: Set<ProcurementMethodModalities>?,

        val electronicAuctions: ElectronicAuctions?
)