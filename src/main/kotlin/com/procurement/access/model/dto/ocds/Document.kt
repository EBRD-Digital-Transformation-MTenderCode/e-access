package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.domain.model.enums.DocumentType

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Document @JsonCreator constructor(

    val id: String,

    val documentType: DocumentType,

    var title: String?,

    var description: String?,

    var relatedLots: MutableList<String>?
)
