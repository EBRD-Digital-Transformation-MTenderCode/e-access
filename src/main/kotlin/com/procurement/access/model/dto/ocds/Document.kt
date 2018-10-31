package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.model.dto.databinding.StringsDeserializer
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Document @JsonCreator constructor(

        val id: String,

//        @field:JsonDeserialize(using = StringsDeserializer::class)
        val documentType: DocumentType,

        var title: String?,

        var description: String?,

        var relatedLots: HashSet<String>?
)
