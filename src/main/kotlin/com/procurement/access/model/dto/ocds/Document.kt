package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.TenderDocumentType
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Document @JsonCreator constructor(

        val id: String,

        val documentType: TenderDocumentType,

        var title: String?,

        var description: String?,

        var relatedLots: HashSet<String>?
)
