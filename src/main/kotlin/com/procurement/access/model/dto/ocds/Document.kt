package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Document @JsonCreator constructor(

        val id: String,

        val documentType: DocumentType,

        var title: String?,

        var description: String?,

        var relatedLots: HashSet<String>?
)
